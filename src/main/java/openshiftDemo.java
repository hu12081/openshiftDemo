import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;

public class openshiftDemo {
  public static void main(String[] args) {

    OpenShiftConfig config = new OpenShiftConfigBuilder()
      .withOpenShiftUrl("https://master.example.com:8443")
      .withMasterUrl("https://master.example.com:8443")
      .withUsername("dev")
      .withPassword("dev")
      .withTrustCerts(true).build();

    OpenShiftClient client = new DefaultOpenShiftClient(config);

    //获取工程列表，必须是集群管理员
    NamespaceList myNs = client.namespaces().list();
    //遍历打印工程名
    for(Namespace ns: myNs.getItems())
      System.out.println(ns.getMetadata().getName());

  }
}

