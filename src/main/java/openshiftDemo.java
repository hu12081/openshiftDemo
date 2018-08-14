import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.URLUtils;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.URL;

public class openshiftDemo {
    private static final String AUTHORIZATION = "Authorization";
    private static final String LOCATION = "Location";
    private static final String AUTHORIZE_PATH = "oauth/authorize?response_type=token&client_id=openshift-challenging-client";

    private static final String BEFORE_TOKEN = "access_token=";
    private static final String AFTER_TOKEN = "&expires";

    public static void main(String[] args) {
        OpenShiftConfig config = new OpenShiftConfigBuilder()
                .withOpenShiftUrl("https://master.example.com:8443")
                .withMasterUrl("https://master.example.com:8443")
                .withUsername("dev")
                .withPassword("dev")
                .withTrustCerts(true).build();
        DefaultOpenShiftClient client = new DefaultOpenShiftClient(config);
        openshiftDemo openshiftDemo=new openshiftDemo();
        //获取用户token
        System.out.println(openshiftDemo.authorize(client.getHttpClient(), config));
        //获取工程列表，这里必须是集群管理员的账号
        NamespaceList myNs = client.namespaces().list();
        //遍历打印工程名
        for (Namespace ns : myNs.getItems())
            System.out.println(ns.getMetadata().getName());
    }

    //获取token
    public String authorize(OkHttpClient client, OpenShiftConfig config) {
        try {
            OkHttpClient.Builder builder = client.newBuilder();
            builder.interceptors().remove(this);
            OkHttpClient clone = builder.build();

            String credential = Credentials.basic(config.getUsername(), new String(config.getPassword()));
            URL url = new URL(URLUtils.join(config.getMasterUrl(), AUTHORIZE_PATH));
            Response response = clone.newCall(new Request.Builder().get().url(url).header(AUTHORIZATION, credential).build()).execute();

            response.body().close();
            response = response.priorResponse() != null ? response.priorResponse() : response;
            response = response.networkResponse() != null ? response.networkResponse() : response;
            String token = response.header(LOCATION);
            if (token == null || token.isEmpty()) {
                throw new KubernetesClientException("Unexpected response (" + response.code() + " " + response.message() + "), to the authorization request. Missing header:[" + LOCATION + "]!");
            }
            token = token.substring(token.indexOf(BEFORE_TOKEN) + BEFORE_TOKEN.length());
            token = token.substring(0, token.indexOf(AFTER_TOKEN));
            return token;
        } catch (Exception e) {
            throw KubernetesClientException.launderThrowable(e);
        }
    }
}
