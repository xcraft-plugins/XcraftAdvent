package de.groovybyte.spigot.xcraftadvent;

public interface IServiceProvider {

    <S> S getService(Class<? extends S> serviceType);
}
