package com.template

import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

/**
 * A nice state used for making arbitrary decisions with a group of people.
 */
@CordaSerializable
class FairDecision(val decision: String, val parties: List<Party>): ContractState {
    override val participants = parties
}
