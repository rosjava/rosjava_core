package ros.android.activity;

import org.ros.service.app_manager.StartApp;

import org.ros.service.app_manager.StopApp;

import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.namespace.NameResolver;
import org.ros.service.app_manager.ListApps;

import java.net.URI;

public class AppManagerIdentifier {

  private final NameResolver resolver;
  private final URI serviceUri;

  public AppManagerIdentifier(NameResolver resolver, URI serviceUri) {
    this.serviceUri = serviceUri;
    this.resolver = resolver;
  }

  public ServiceIdentifier getListAppsIdentifier() {
    ListApps serviceMeta = new ListApps();
    ServiceDefinition serviceDefinition = new ServiceDefinition(new GraphName(
        resolver.resolveName("list_apps")), serviceMeta.getDataType(), serviceMeta.getMD5Sum());
    return new ServiceIdentifier(serviceUri, serviceDefinition);
  }

  public ServiceIdentifier getStartAppIdentifier() {
    StartApp serviceMeta = new StartApp();
    ServiceDefinition serviceDefinition = new ServiceDefinition(new GraphName(
        resolver.resolveName("start_app")), serviceMeta.getDataType(), serviceMeta.getMD5Sum());
    return new ServiceIdentifier(serviceUri, serviceDefinition);
  }

  public ServiceIdentifier getStopAppIdentifier() {
    StopApp serviceMeta = new StopApp();
    ServiceDefinition serviceDefinition = new ServiceDefinition(new GraphName(
        resolver.resolveName("stop_app")), serviceMeta.getDataType(), serviceMeta.getMD5Sum());
    return new ServiceIdentifier(serviceUri, serviceDefinition);
  }

}
