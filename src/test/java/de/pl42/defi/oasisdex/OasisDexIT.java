package peggy42.cn.oasisdex;

import peggy42.cn.compounddai.CompoundDai;
import peggy42.cn.contractneedsprovider.*;
import peggy42.cn.dai.Dai;
import peggy42.cn.gasprovider.GasProvider;
import peggy42.cn.util.JavaProperties;
import peggy42.cn.weth.Weth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OasisDexIT {
  private static final BigInteger minimumGasPrice = BigInteger.valueOf(1_000000000);
  private static final BigInteger maximumGasPrice = BigInteger.valueOf(200_000000000L);
  OasisDex oasisDex;

  @BeforeEach
  public void setUp() {
    JavaProperties javaProperties = new JavaProperties(true);
    String ethereumAddress = javaProperties.getValue("myEthereumAddress");
    String password = javaProperties.getValue("password");
    String infuraProjectId = javaProperties.getValue("infuraProjectId");
    CircuitBreaker circuitBreaker = new CircuitBreaker();
    Web3j web3j = new Web3jProvider(infuraProjectId).web3j;
    Credentials credentials = new Wallet(password, ethereumAddress, true).getCredentials();
    GasProvider gasProvider = new GasProvider(web3j, minimumGasPrice, maximumGasPrice);
    Permissions permissions = new Permissions(true, true);
    ContractNeedsProvider contractNeedsProvider =
        new ContractNeedsProvider(web3j, credentials, gasProvider, permissions, circuitBreaker);
    Weth weth = new Weth(contractNeedsProvider);
    CompoundDai compoundDai = new CompoundDai(contractNeedsProvider);
    oasisDex = new OasisDex(contractNeedsProvider, compoundDai, weth);
  }

  @Test
  public void isContractValid_isValid_continueRunning() {
    oasisDex.isContractValid();
  }

  @Test
  public void isContractValid_isNotValid_stopRunning() {}

  @Test
  public void getOffer_nonExistingOffer_DaiOrWethMissingException() {
    Exception exception =
        assertThrows(DaiOrWethMissingException.class, () -> oasisDex.getOffer(BigInteger.ZERO));
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains("BOTH DAI AND WETH NEED TO BE PRESENT ONCE."));
  }

  @Test
  public void getBestOffer_buyDai_returnOffer() {
    BigInteger actual = oasisDex.getBestOffer(Dai.ADDRESS, Weth.ADDRESS);
    assertNotEquals(BigInteger.ZERO, actual);
    assertNotNull(actual);
  }

  @Test
  public void getBestOffer_sellDai_returnOffer() {
    BigInteger actual = oasisDex.getBestOffer(Weth.ADDRESS, Dai.ADDRESS);
    assertNotEquals(BigInteger.ZERO, actual);
    assertNotNull(actual);
  }

  @Test
  public void buyDaiSellWethIsProfitable_triggerException_emptyOasisDexOffer() {
    //    oasisDex.buyDaiSellWethIsProfitable();
  }

  @Test
  public void buyDaiSellWethIsProfitable_realValues_OasisDexOffer() {}

  @Test
  public void sellDaiBuyWethIsProfitable_triggerException_emptyOasisDexOffer() {}

  @Test
  public void sellDaiBuyWethIsProfitable_realValues_OasisDexOffer() {}
}
