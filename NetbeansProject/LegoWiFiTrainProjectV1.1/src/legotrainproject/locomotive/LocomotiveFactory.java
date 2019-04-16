/*
 * Not licensed yet, use at your own risk, no warrenties!
 */
package legotrainproject.locomotive;

/**
 *
 * @author Tobias Grundtvig <tgrundtvig@gmail.com>
 */
public interface LocomotiveFactory
{
    public Locomotive newLocomotive(long deviceId);
    public void addListener(LocomotiveFactoryListener listener);
    public boolean removeListener(LocomotiveFactoryListener listener);
}
