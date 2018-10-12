package com.template

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.CompositeKey
import net.corda.core.transactions.LedgerTransaction

class DecisionContract: Contract {
    companion object {
        @JvmStatic
        val ID = "com.template.DecisionContract"
    }

    interface Commands: CommandData {
        class Create: Commands
    }

    override fun verify(tx: LedgerTransaction) {
        requireThat {
            val command = tx.commands.requireSingleCommand<Commands.Create>()
            "Can only be one signer." using (command.signers.size == 1)
            "The required signer is a CompositeKey." using (command.signers[0] is CompositeKey)
            val signer = command.signers.first() as CompositeKey

            "There are at least 3 people." using (signer.children.size > 2)

            // Rounded up, at minimum 50% of the participants must sign
            "The threshold is minimum 50% of participants." using (signer.threshold >= Math.ceil(signer.children.size.toDouble() / 2).toInt())
            "There is only one output." using (tx.outputStates.size == 1)
            "The output is of type FairDecision." using (tx.outputsOfType<FairDecision>().size == 1)
            val output = tx.outRefsOfType<FairDecision>().single().state.data
            val expectedNodesAndWeights = output.parties.map {
                CompositeKey.NodeAndWeight(it.owningKey, 1)
            }
            "Every party has an equal weight." using (signer.children.containsAll(expectedNodesAndWeights))
        }
    }
}
