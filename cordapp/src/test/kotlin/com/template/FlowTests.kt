package com.template


import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail


private val NOTARY_NAME = CordaX500Name(organisation = "Kingdom of Humans", locality = "New York", country = "US")
private val HUMANA = CordaX500Name(organisation = "Conglomerate of Humans", locality = "Seatle", country = "US")
private val HUMANB = CordaX500Name(organisation = "Federation of Humans", locality = "Nukuʻalofa", country = "TO")
private val HUMANC = CordaX500Name(organisation = "Humanity Dukedom", locality = "Sydney", country = "AU")

class FlowTests {

    private lateinit var mockNet: MockNetwork
    private lateinit var notary: StartedMockNode
    private lateinit var humanA: StartedMockNode
    private lateinit var humanB: StartedMockNode
    private lateinit var humanC: StartedMockNode


    @Before
    fun setup() {
        mockNet = MockNetwork(cordappPackages = listOf("com.template"), notarySpecs = listOf(MockNetworkNotarySpec(NOTARY_NAME, validating = false)))
        notary = mockNet.defaultNotaryNode
        humanA = mockNet.createNode(legalName = HUMANA)
        humanB = mockNet.createNode(legalName = HUMANB)
        humanC = mockNet.createNode(legalName = HUMANC)
        mockNet.runNetwork()
    }

    @After
    fun tearDown() = mockNet.stopNodes()

    @Test
    fun `create decision`() {
        val dec = "Play nep"
        val flow = CreateDecision(
                dec,
                mutableListOf(
                        humanB.info.singleIdentity(),
                        humanC.info.singleIdentity()
                )
        )
        val future = humanA.startFlow(flow)
        mockNet.runNetwork()
        val result = future.getOrThrow()
        assertEquals(result.decision, dec)
    }

    @Test
    fun `fail create decision`() {
        val dec = "Should we invest in Crypto?"
        val flow = CreateDecision(
                dec,
                mutableListOf(
                        humanB.info.singleIdentity(),
                        humanC.info.singleIdentity()
                )
        )
        try {
            val future = humanA.startFlow(flow)
            mockNet.runNetwork()
            future.getOrThrow()
        } catch (e: FlowException) {
            assert(e.originalMessage?.startsWith("Contract verification failed") ?: false)
        }
        fail()
    }
}