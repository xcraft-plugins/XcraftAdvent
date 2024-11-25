package de.groovybyte.spigot.xcraftadvent;

public interface IServiceProvider {

	public <S> S getService(Class<? extends S> serviceType);
}
