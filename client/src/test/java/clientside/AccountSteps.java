package clientside;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class AccountSteps {

    User customer;
    BankService bank = new BankServiceService().getBankServicePort();
    List<String> accountIds = new ArrayList<>();
    List<User> registeredUsers = new ArrayList<>();
    AccountService accountService = new AccountService();
    dtu.ws.fastmoney.User user = new dtu.ws.fastmoney.User();
    boolean successful;
    UserInfo userInfo = new UserInfo();


    public AccountSteps(){
    }

    @Given("a new customer with name {string} {string} and CPR {string}")
    public void aNewCustomerWithNameAndCPR(String firstName, String lastName, String cpr) throws Exception {
        System.out.println(cpr);
        customer = new Customer(firstName, lastName, cpr, "1",false);
    }

    @Given("the customer has a bank account")
    public void theCustomerHasABankAccount() throws Exception {
       // UUID uuid = UUID.randomUUID();
        user.setFirstName(customer.getFirstName());
        user.setLastName(customer.getLastName());
        user.setCprNumber(customer.getCprNumber());

        try {
            String accountId = bank.createAccountWithBalance(user ,new BigDecimal(1000));
            accountIds.add(accountId);
            System.out.println("created bank account for customer " + customer.getCprNumber());
        } catch (BankServiceException_Exception e) {
            retireAccounts();
            e.printStackTrace();
            throw new Exception();
        }
    }


    @When("the user initiates registration as a customer {string}")
    public void theUserInitiatesRegistrationAsACustomer(String userType) {
        System.out.println(userType);
        try {
            successful = accountService.register(customer);
            registeredUsers.add(customer);
        } catch (Exception e) {
            successful = false;
            e.printStackTrace();
        }
        //        Response response = baseUrl.path("Account/User").request()
//                .put(Entity.entity(userInfo, MediaType.APPLICATION_JSON));
//        System.out.println(userInfo.getCprNumber());
//        System.out.println(response.getStatus() +" " +  Response.Status.OK.getStatusCode());
//        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Then("registration of customer is successful")
    public void registrationOfCustomerIsSuccessful() {
        assertTrue(successful);
    }

    @After
    public void retireAccounts()  {
        try{
            for (String id : accountIds){
                bank.retireAccount(id);
                System.out.println("retired account "+ id);
            }
            accountIds.clear();
            System.out.println(registeredUsers.size());
            for (User user : registeredUsers) {
                accountService.deregister(user);
            }
            registeredUsers.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
