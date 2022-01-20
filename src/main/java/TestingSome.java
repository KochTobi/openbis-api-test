import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class TestingSome {

//  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestingSome.class);

  public static void main(String[] args) {
    String applicationServerUrl = System.getenv("OPENBIS_URL");
    long serverTimeout = 1000;
    String openbisUser = System.getenv("OPENBIS_USER");
    String openbisPassword = System.getenv("OPENBIS_PASSWORD");

    IApplicationServerApi openBisApplicationServerApi = HttpInvokerUtils.createServiceStub(
        IApplicationServerApi.class,
        applicationServerUrl + IApplicationServerApi.SERVICE_URL, serverTimeout);

    String sessionToken = openBisApplicationServerApi.login(openbisUser, openbisPassword);

    Objects.requireNonNull(sessionToken);

    getUpdatedSamples(LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC), openBisApplicationServerApi, sessionToken);
  }

  private static void getUpdatedSamples(Instant updatedSince,
      IApplicationServerApi openBisApplicationServerApi, String sessionToken) {
    SampleSearchCriteria criteria = new SampleSearchCriteria();
    criteria.withModificationDate().thatIsLaterThanOrEqualTo(Date.from(updatedSince));

    // we need to fetch properties
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withProperties();

    SearchResult<Sample> result = null;
    // it works until here. The session token is generated.
    try {
      result = openBisApplicationServerApi.searchSamples(sessionToken, criteria, fetchOptions);
      //the searchSamples method throws an java.lang.reflect.InvocationTargetException caused by a NullPointer
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      Throwable e = undeclaredThrowableException.getUndeclaredThrowable();
      System.err.println(e.getMessage());
      System.exit(1);
    }

    Objects.requireNonNull(result, "result must not be null");
    List<Sample> resultingList = result.getObjects().stream().map(it -> (Sample) it)
        .collect(Collectors.toList());
    System.out.println("resultingList = " + resultingList);
  }

}
