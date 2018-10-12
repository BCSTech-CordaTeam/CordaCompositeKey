package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.deserialize
import net.corda.core.serialization.serialize
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.lang.Math.ceil
import java.security.PublicKey


@InitiatingFlow
@StartableByRPC
class CreateDecision(val decision: String, val otherParties: MutableList<Party>): FlowLogic<FairDecision>() {

    @Suspendable
    override fun call(): FairDecision {

        logger.info("Starting CreateDecision with '$decision'")
        // We want at least 50% of the parties to accept the decision.
        // Fairness ♥
        val threshold: Int = ceil(otherParties.size.toDouble() / 2).toInt()

        val keys: List<PublicKey> = otherParties.map { it.owningKey }

        // Create a composite key from the participant keys.
        val compositeKey = CompositeKey.Builder()
                .addKey(ourIdentity.owningKey, weight = 1)
                .addKeys(keys) // Weights default to 1
                .build(threshold)

        val everyone = otherParties.subList(0, otherParties.size)
        everyone.add(ourIdentity)

        val state = FairDecision(decision, everyone)

        // Composite key is used as the required signer. It will be properly signed if the threshold is met.
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addCommand(DecisionContract.Commands.Create(), compositeKey)
                .addOutputState(state, DecisionContract.ID)

        val partStx = serviceHub.signInitialTransaction(txBuilder)

        // Collecting signatures
        val sessions = otherParties.map { initiateFlow(it) }

        val signatures = sessions
                .map { it.sendAndReceive<Any>(partStx).unwrap { it } }
                .filter { it is TransactionSignature } // We only care about TransactionSignature responses.
                .map { it as TransactionSignature }
        val fullStx = partStx.withAdditionalSignatures(signatures)

        subFlow(FinalityFlow(fullStx))
        return state
    }
}

@InitiatedBy(CreateDecision::class)
class AcceptDecision(val otherPartySession: FlowSession): FlowLogic<Unit>() {

    // Some decisions we agree with and will sign.
    private val goodDecisions = listOf(
            "Purchase a miku figurine",
            "Play nep",
            "Watch overlord",
            "Become grandmaster in Starcraft"
    )

    @Suspendable
    override fun call() {
        otherPartySession.receive<SignedTransaction>().unwrap { partStx ->
            // We return either a `TransactionSignature` (if we're willing
            // to sign) or `false`.
            val decision = partStx.coreTransaction.outputsOfType<FairDecision>().first().decision

            logger.info("Checking decision: $decision")

            val response = if(decision in goodDecisions) {
                logger.info("Accepting decision")
                serviceHub.createSignature(partStx)
            } else {
                logger.info("Denying decision")
                false
            }
            otherPartySession.send(response)
        }
    }
}
