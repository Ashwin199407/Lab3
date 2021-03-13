import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;


/** 
 *   AccountBalance defines an accountBalance in the ledger model of bitcoins
 */

public class AccountBalance {

    /** 
     * The current accountBalance, with each account's public Key mapped to its 
     *    account balance.
     */
    
    private Hashtable<PublicKey, Integer> accountBalanceBase;

    /**
     *  In order to print out the accountBalance in a good order
     *  we maintain a list of public Keys,
     *  which will be the set of public keys maped by it in the order
     *  they were added
     **/

    private ArrayList<PublicKey> publicKeyList;


    /** 
     * Creates a new accountBalance
     */
    public AccountBalance() {
	accountBalanceBase = new Hashtable<PublicKey, Integer>();
	publicKeyList = new ArrayList<PublicKey>();
	
    }

    /** 
     * Creates a new accountBalance from a map from string to integers
     */
    
    public AccountBalance(Hashtable<PublicKey, Integer> accountBalanceBase) {
	this.accountBalanceBase = accountBalanceBase;
	publicKeyList = new ArrayList<PublicKey>();	
	for (PublicKey pbk : accountBalanceBase.keySet()){
	    publicKeyList.add(pbk);
	}
    }

    /** obtain the underlying Hashtable from string to integers
     */   
    
    public Hashtable<PublicKey,Integer> getAccountBalanceBase(){
	return accountBalanceBase;
    };

    /** 
      * obtain the list of publicKeys in the tree map
      */   
    
    public Set<PublicKey> getPublicKeys(){
	return getAccountBalanceBase().keySet();
    };

    /** 
      * obtain the list of publicKeys in the order they were added
      */   

    public ArrayList<PublicKey> getPublicKeysOrdered(){
	return publicKeyList;
    };        

    
    

    /** 
     * Adds a mapping from new account's name {@code publicKey} to its 
     * account balance {@code balance} into the accountBalance. 
     *
     * if there was an entry it is overridden.  
     */

    public void addAccount(PublicKey publicKey, int balance) {
	accountBalanceBase.put(publicKey, balance);
	if (! publicKeyList.contains(publicKey)){
	    publicKeyList.add(publicKey);
	}
    }

    /** 
     * @return true if the {@code publicKey} exists in the accountBalance.
     */
    
    public boolean hasPublicKey(PublicKey publicKey) {
	return accountBalanceBase.containsKey(publicKey);
    }


    /** 
     * @return the balance for this account {@code account}
     *
     *  if there was no entry, return zero
     *
     */
    
    public int getBalance(PublicKey publicKey) {
	if (hasPublicKey(publicKey)){
		return accountBalanceBase.get(publicKey);
	    } else
	    {  return 0;
	    }
    }


    /** 
     * set the balance for {@code publicKey} to {@code amount}
     */

    
    public void setBalance(PublicKey publicKey, int amount){
	accountBalanceBase.put(publicKey,amount);
	if (! publicKeyList.contains(publicKey)){
	    publicKeyList.add(publicKey);
	}	
	    };
	

    /** 
     * Imcrements Adds amount to balance for {@code publicKey}
     * 
     *  if there was no entry for {@code publicKey} add one with 
     *       {@code balance}
     */
    
    public void addToBalance(PublicKey publicKey, int amount) {
	setBalance(publicKey,getBalance(publicKey) + amount);
    }


    /** 
     * Subtracts amount from balance for {@code publicKey}
     */
    
    public void subtractFromBalance(PublicKey publicKey, int amount) {
	setBalance(publicKey,getBalance(publicKey) - amount);
    }


    /** 
     * Check balance has at least amount for {@code publicKey}
     */
    public boolean checkBalance(PublicKey publicKey, int amount) {
	return (getBalance(publicKey) >= amount);
    }


    /* checks whether an accountBalance can be deducted 
       this is an auxiliary function used to define checkTxInputListCanBeDeducted */

    public boolean checkAccountBalanceCanBeDeducted(AccountBalance accountBalance2){
	for (PublicKey publicKey : accountBalance2.getPublicKeys()) {
	    if (getBalance(publicKey) < accountBalance2.getBalance(publicKey))
		return false;
	};
	return true;
    };


    /** 
     *  Check that a list of publicKey amounts can be deducted from the 
     *     current accountBalance
     *
     *   done by first converting the list of publicKey amounts into an accountBalance
     *     and then checking that the resulting accountBalance can be deducted.
     *   
     */    


    public boolean checkTxInputListCanBeDeducted(TxInputList txInputList){
	return checkAccountBalanceCanBeDeducted(txInputList.toAccountBalance());
    };


    /** 
     * Subtract a list of TxInput from the accountBalance
     *
     *   requires that the list to be deducted is deductable.
     *   
     */    
    

    public void subtractTxInputList(TxInputList txInputList){
	for (TxInput entry : txInputList.toList()){
	    subtractFromBalance(entry.getSender(),entry.getAmount());
	}
    }


    
    /** 
     * Adds a list of txOutput of a transaction to the current accountBalance
     *
     */    

    public void addTxOutputList(TxOutputList txOutputList){
	for (TxOutput entry : txOutputList.toList()){
	    addToBalance(entry.getRecipient(),entry.getAmount());
	}
    }


    /** 
     *
     *  Task 4 Check a transaction is valid.
     *
     *  this means that 
     *    the sum of outputs is less than the sum of inputs
     *    all signatures are valid
     *    and the inputs can be deducted from the accountBalance.

     *    This method has been set to true so that the code compiles - that should
     *    be changed
     */    

    public boolean checkTransactionValid(Transaction tx){
	return(tx.checkTransactionAmountsValid() && tx.checkSignaturesValid() && checkTxInputListCanBeDeducted(tx.toTxInputs()));
	
    };


    /** 
     * Process a transaction
     *    by first deducting all the inputs
     *    and then adding all the outputs.
     *
     */    
    
    public void processTransaction(Transaction tx){
	subtractTxInputList(tx.toTxInputs());
	addTxOutputList(tx.toTxOutputs());
    };

    
    /** 
     * Prints the current state of the accountBalance. 
     */

    public void print(PublicKeyMap pubKeyMap) {
	for (PublicKey publicKey : publicKeyList ) {
	    Integer value = getBalance(publicKey);
	    System.out.println("The balance for " +
			       pubKeyMap.getUser(publicKey) + " is " + value); 
	}

    }



    /** 
     * Testcase
     */

    public static void test()
	throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

	Wallet exampleWallet = SampleWallet.generate(new String[]{ "Alice"});
	byte[] exampleMessage = KeyUtils.integer2ByteArray(1);
	byte[] exampleSignature = exampleWallet.signMessage(exampleMessage,"Alice");

	/*   Task 5
    add  to the test case the test as described in the lab sheet
     
    you can use the above exampleSignature, when a sample signature is needed
which cannot be computed from the data.

**/

System.out.println("\r\n Task 5.1 Create a sample wallet for Alice containing keys with names A1 and A2, for Bob\n"
	+ "containing keynames B1 and B2, for Carol containing keynames C1, C2, C3, and for\n"
	+ "David containing keyname D1 by using the method 'generate' of the class SampleWallet.\r\n");

Wallet Alicewallet = SampleWallet.generate(new String[]{"A1","A2"});
Wallet Bobwallet   = SampleWallet.generate(new String[]{"B1","B2"});
Wallet Carolwallet = SampleWallet.generate(new String[]{"C1","C2","C3"});
Wallet Davidwallet = SampleWallet.generate(new String[]{"D1"});

System.out.println();

System.out.println("\r\n Task 5.2 : Compute the PublicKeyMap containing the public keys of all these wallets. The\n"
	+ "PublicKeyMap is for convenience since comparing public keys is cumbersome. \r\n");

PublicKeyMap pubKeyMapAlice = Alicewallet.toPublicKeyMap();
PublicKeyMap pubKeyMapBob = Bobwallet.toPublicKeyMap();
PublicKeyMap pubKeyMapCarol = Carolwallet.toPublicKeyMap();
PublicKeyMap pubKeyMapDavid = Davidwallet.toPublicKeyMap();

PublicKeyMap pubkeymapabcd = new PublicKeyMap();
pubkeymapabcd.addPublicKeyMap(pubKeyMapAlice);
pubkeymapabcd.addPublicKeyMap(pubKeyMapBob);
pubkeymapabcd.addPublicKeyMap(pubKeyMapCarol);
pubkeymapabcd.addPublicKeyMap(pubKeyMapDavid);

	
 
PublicKey pubKeyA1 =	pubKeyMapAlice.getPublicKey("A1");

PublicKey pubKeyA2 =	pubKeyMapAlice.getPublicKey("A2"); 

PublicKey pubKeyB1 =	pubKeyMapBob.getPublicKey("B1"); 

PublicKey pubKeyB2 =	pubKeyMapBob.getPublicKey("B2"); 

PublicKey pubKeyC1 =	pubKeyMapCarol.getPublicKey("C1");

PublicKey pubKeyC2 =	pubKeyMapCarol.getPublicKey("C2"); 

PublicKey pubKeyC3 =	pubKeyMapCarol.getPublicKey("C3"); 

PublicKey pubKeyD1 =	pubKeyMapDavid.getPublicKey("D1"); 

System.out.println();


System.out.println("\r\n Task 5.3 : Create an empty AccountBalance and add to it the keynames of the wallets created before initialised with the amount 0 for each key.\n"
	+ " \r\n");
AccountBalance AccBal = new AccountBalance();
AccBal.addAccount(pubKeyA1, 0);
AccBal.addAccount(pubKeyA2, 0);
AccBal.addAccount(pubKeyB1, 0);
AccBal.addAccount(pubKeyB2, 0);
AccBal.addAccount(pubKeyC1, 0);
AccBal.addAccount(pubKeyC2, 0);
AccBal.addAccount(pubKeyC3, 0);
AccBal.addAccount(pubKeyD1, 0);
AccBal.print(pubkeymapabcd);
System.out.println();

System.out.println("\r\n Task 5.4 :Set the balance for A1 to 20.");
AccBal.setBalance(pubKeyA1, 20);
AccBal.print(pubkeymapabcd);
System.out.println();

System.out.println("\r\n Task 5.5 : Add 15 to the balance for B1");
AccBal.addToBalance(pubKeyB1,15);
AccBal.print(pubkeymapabcd);
System.out.println();

//5.6
System.out.println("\r\n Task 5.6 : Subtract 5 from the balance for B1.");
AccBal.subtractFromBalance(pubKeyB1,5);
AccBal.print(pubkeymapabcd);
System.out.println();

//5.7
System.out.println("\r\n Task 5.7 : Set the balance for C1 to 10.");
AccBal.setBalance(pubKeyC1, 10);
AccBal.print(pubkeymapabcd);
System.out.println();

//5.8
System.out.println("\r\n Task 5.8 : Check whether the TxInputList txil1 giving A1 15 units, and B1 5 units (with\n"
	+ "sample signatures used) can be deducted.\r\n");
TxInputList txil1 = (new TxInputList(pubKeyA1,15,exampleSignature,pubKeyB1,5,exampleSignature));   
txil1.testCase("Test Alice 15 & Bob 5", pubkeymapabcd);
boolean tx1 = AccBal.checkTxInputListCanBeDeducted(txil1);
System.out.println("can txil1 be deducted?? : "+tx1);
System.out.println();


//5.9
System.out.println("\r\n Task 5.9 : Check whether the TxInputList txil2 giving A1 15 units, and giving A1 again\n"
	+ "15 units can be deducted.\r\n");
TxInputList txil2 = (new TxInputList(pubKeyA1,15,exampleSignature,pubKeyA1,15,exampleSignature));	    
txil2.testCase("Test Alice 15&15", pubkeymapabcd);
boolean tx2 = AccBal.checkTxInputListCanBeDeducted(txil2);
System.out.println("can txil2 be deducted?? : "+tx2);
System.out.println();

//6.0
System.out.println("\r\n Task 6.0 : Deduct txil1 from the AccountBalance.\r\n");
AccBal.subtractTxInputList(txil1);
AccBal.print(pubkeymapabcd);
System.out.println();

//6.1
System.out.println("\r\n Taask 6.1 : Create a TxOutputList corresponding to txil2 which gives A1 twice 15 Units,\n"
	+ "and add it to the AccountBalance.\n"
	+ " \r\n");
TxOutputList txol1 = (new TxOutputList(pubKeyA1,15,pubKeyA1,15));    
AccBal.addTxOutputList(txol1);
AccBal.print(pubkeymapabcd);
System.out.println();

//6.2
System.out.println("\r\n Task 6.2 :  Create a correctly signed input, where A1 is spending 30, referring to an output\n"
	+ "list giving B2 10 and C1 20. The output list is needed in order to create the\n"
	+ "message to be signed (consisting of A1 spending 30, B1 receiving 10 and C1\n"
	+ "receiving 20). Check whether the signature is valid for this signed input. \r\n");
TxOutputList txol2 = (new TxOutputList(pubKeyB2,10,pubKeyC1,20));	    
TxInputUnsigned TIU = new TxInputUnsigned(pubKeyA1, 30);
byte[] message = TIU.getMessageToSign(txol2);
byte[] A1Signature = Alicewallet.signMessage(message,"A1");
TxInputList txil3 = (new TxInputList(pubKeyA1,30,A1Signature));	 
boolean valid = txil3.checkSignature(txol2);
System.out.println("Is the signature valid for signed input?  : "+valid);	
System.out.println();

//6.3	 
System.out.println("\r\n Task 6.3 : Create a wrongly signed input, which gives A1 30, and uses instead of the correctly\n"
	+ "created signature an example signature (example signatures are provided in the\n"
	+ "code). Check whether the signature is valid for this signed input. \r\n");
TxInputList txil4 = (new TxInputList(pubKeyA1,30,exampleSignature));
boolean valid_check = txil4.checkSignature(txol2);
System.out.println("Is the signature valid for signed input?  : "+valid_check);
System.out.println();

//6.4
System.out.println("\r\n TAsk 6.4 : Create a transaction tx1 which takes as input for A1 35 units and gives B2 10,\n"
	+ "C2 10, and returns the change (whatever is left) to A2. \r\n");
TxOutputList TxOuLi = new TxOutputList(pubKeyB2,10,pubKeyC2,10,pubKeyA2,15);
TxInputUnsigned Tiu1 = new TxInputUnsigned(pubKeyA1, 35);
byte[] A1message = Tiu1.getMessageToSign(TxOuLi);
byte[] signA1 = Alicewallet.signMessage(A1message,"A1" );
TxInputList txil5 = (new TxInputList(pubKeyA1,35,signA1));	
Transaction tx = new Transaction(txil5,TxOuLi);
tx.print(pubkeymapabcd);
System.out.println();

System.out.println("\r\n Task 6.5 :Check whether the signature is approved for the transaction input, and whether\n"
+ "the transaction is valid. Then update the AccountBalance using that transaction. \r\n");
if( AccBal.checkTransactionValid(tx))  {
System.out.println("Signature is approved for transaction input and the transaction is valid");
AccBal.processTransaction(tx);
System.out.println("Updated Account Balance details:");
AccBal.print(pubkeymapabcd);
System.out.println();
}else {
System.out.println("Transaction is invalid");
}

System.out.println("\r\n Task 6.1 : Create a transaction tx2 which takes as inputs from B2 10, C2 10, and as outputs\n"
+ "given D1 15 and C3 the change (whatever is left) \r\n");
TxOutputList TxOuLi1 = new TxOutputList(pubKeyD1,15,pubKeyC3,5);
TxInputUnsigned Tiu2 = new TxInputUnsigned(pubKeyB2, 10);
byte[] B2message = Tiu2.getMessageToSign(TxOuLi1);
byte[] signB2 = Bobwallet.signMessage(B2message,"B2" );	   
TxInputUnsigned Tiu3 = new TxInputUnsigned(pubKeyC2, 10);
byte[] C2message = Tiu3.getMessageToSign(TxOuLi1);
byte[] signC2 = Carolwallet.signMessage(C2message,"C2" );	   
TxInputList txil6 = (new TxInputList(pubKeyB2,10,signB2,pubKeyC2,10,signC2));	
Transaction trx = new Transaction(txil6,TxOuLi1);
trx.print(pubkeymapabcd);
System.out.println();

System.out.println("\r\n Task 6.2 : Check whether the signature is approved for the transaction input, and whether\n"
+ "the transaction is valid. Then update the AccountBalance using that transaction. \r\n");
if( AccBal.checkTransactionValid(trx)) {
System.out.println("Signature is approved for transaction input and the transaction is valid");
AccBal.processTransaction(trx);
System.out.println("Updated Account Balance details:");
AccBal.print(pubkeymapabcd);
}else {
System.out.println("Transaction is invalid");
}




}

/** 
* main function running test cases
*/            

public static void main(String[] args)
throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
AccountBalance.test();
}
}
