/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableMember
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.scopes.ProcessorAction.NEXT
import org.jetbrains.kotlin.fir.scopes.ProcessorAction.STOP
import org.jetbrains.kotlin.fir.symbols.AbstractFirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.ConeCallableSymbol
import org.jetbrains.kotlin.fir.symbols.ConeFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.ConePropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast

class FirClassUseSiteScope(
    session: FirSession,
    val superTypesScope: FirScope,
    val declaredMemberScope: FirClassDeclaredMemberScope,
    lookupInFir: Boolean
) : FirAbstractProviderBasedScope(session, lookupInFir) {


    //base symbol as key
    val overrides = mutableMapOf<ConeCallableSymbol, ConeCallableSymbol?>()


    fun isSubtypeOf(subType: ConeKotlinType, superType: ConeKotlinType) = true
    fun isSubtypeOf(subTypeRef: FirTypeRef, superTypeRef: FirTypeRef) =
        //TODO: Discuss
        isSubtypeOf(subTypeRef.cast<FirResolvedTypeRef>().type, superTypeRef.cast<FirResolvedTypeRef>().type)

    fun isEqualTypes(a: ConeKotlinType, b: ConeKotlinType) = true
    fun isEqualTypes(a: FirTypeRef, b: FirTypeRef) = isEqualTypes(a.cast<FirResolvedTypeRef>().type, b.cast<FirResolvedTypeRef>().type)

    fun isOverriddenFunCheck(member: FirNamedFunction, self: FirNamedFunction): Boolean {
        return member.valueParameters.size == self.valueParameters.size &&
                member.valueParameters.zip(self.valueParameters).all { (memberParam, selfParam) ->
                    isEqualTypes(memberParam.returnTypeRef, selfParam.returnTypeRef)
                }
    }

    fun ConeCallableSymbol.isOverridden(seen: Set<ConeCallableSymbol>): ConeCallableSymbol? {
        if (overrides.containsKey(this)) return overrides[this]

        fun sameReceivers(memberTypeRef: FirTypeRef?, selfTypeRef: FirTypeRef?): Boolean {
            return when {
                memberTypeRef != null && selfTypeRef != null -> isEqualTypes(memberTypeRef, selfTypeRef)
                else -> memberTypeRef == null && selfTypeRef == null
            }
        }

        fun similarFunctionsOrBothProperties(member: FirCallableMember, self: FirCallableMember): Boolean {
            return when (member) {
                is FirNamedFunction -> self is FirNamedFunction && isOverriddenFunCheck(member, self)
                is FirConstructor -> false
                is FirProperty -> self is FirProperty
                else -> error("Unknown fir callable type: $member, $self")
            }
        }

        val self = (this as AbstractFirBasedSymbol<FirCallableMember>).fir
        val overriding = seen.filter {
            val member = (it as AbstractFirBasedSymbol<FirCallableMember>).fir
            member.isOverride && self.modality != Modality.FINAL
                    && sameReceivers(member.receiverTypeRef, self.receiverTypeRef)
                    && isSubtypeOf(member.returnTypeRef, self.returnTypeRef)
                    && similarFunctionsOrBothProperties(member, self)
        }.firstOrNull() // TODO: WTF when there is two overrides for one fun? More fun
        overrides[this] = overriding
        return overriding
    }

    override fun processFunctionsByName(name: Name, processor: (ConeFunctionSymbol) -> ProcessorAction): ProcessorAction {
        val seen = mutableSetOf<ConeCallableSymbol>()
        if (!declaredMemberScope.processFunctionsByName(name) {
                seen += it
                processor(it)
            }
        ) return STOP

        return superTypesScope.processFunctionsByName(name) {

            val overriddenBy = it.isOverridden(seen)
            if (overriddenBy == null) {
                processor(it)
            } else {
                NEXT
            }
        }
    }

    override fun processPropertiesByName(name: Name, processor: (ConePropertySymbol) -> ProcessorAction): ProcessorAction {
        val seen = mutableSetOf<ConeCallableSymbol>()
        if (!declaredMemberScope.processPropertiesByName(name) {
                seen += it
                processor(it)
            }
        ) return STOP

        return superTypesScope.processPropertiesByName(name) {

            val overriddenBy = it.isOverridden(seen)
            if (overriddenBy == null) {
                processor(it)
            } else {
                NEXT
            }
        }
    }
}


