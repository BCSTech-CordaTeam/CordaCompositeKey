![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# CorDapp Template

This is an example Corda project using composite keys.

Decisions can be made with an arbitrary number of parties, and at minimum 50% of them need to sign the transaction.

## Pre-Requisites

You will need the following installed on your machine before you can start:

* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  installed and available on your path (Minimum version: 1.8_131).


## Building the CorDapp:

**Unix:** 

     ./gradlew deployNodes

**Windows:**

     gradlew.bat deployNodes


## Running the Nodes

Once the build finishes, change directories to the folder where the newly
built nodes are located:

     cd build/nodes

The Gradle build script will have created a folder for each node. You'll
see three folders, one for each node and a `runnodes` script. You can
run the nodes with:

**Unix:**

     ./runnodes

**Windows:**

    runnodes.bat

You should now have three Corda nodes running on your machine serving 
the template.


## Helpful links

- https://stackoverflow.com/questions/51862641/in-corda-how-to-use-a-compositekey-as-a-required-signer
- https://docs.corda.net/releases/release-M7.0/transaction-data-types.html#id2
