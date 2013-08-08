package org.jboss.as.quickstarts.datagrid.carmart.jsf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.as.quickstarts.datagrid.carmart.model.Car;
import org.jboss.as.quickstarts.datagrid.carmart.model.Car.CarType;
import org.jboss.as.quickstarts.datagrid.carmart.model.Car.Country;
import org.jboss.as.quickstarts.datagrid.carmart.session.CacheContainerProvider;
import org.jboss.as.quickstarts.datagrid.carmart.session.CarManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Populates a cache with initial data. We need to obtain BeanManager from JNDI and create an instance of CacheContainerProvider
 * manually since injection into Listeners is not supported by CDI specification.
 * 
 */
public class PopulateCache implements SystemEventListener {

    private Logger log = Logger.getLogger(this.getClass().getName());

    private CacheContainerProvider provider;

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        provider = getContextualInstance(getBeanManager(), CacheContainerProvider.class);
        startup();
    }

    public void startup() {
    	RemoteCache<String, Object> cars = provider.getCacheContainer().getCache(CarManager.CACHE_NAME);
        List<String> carNumbers = new ArrayList<String>();

        try {
            Car c = new Car("Ford Focus", 1.6, CarType.COMBI, "white", "FML 23-25", Country.CHN);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("BMW X3", 2.0, CarType.SEDAN, "gray", "1P3 2632", Country.CHN);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Ford Mondeo", 2.2, CarType.COMBI, "blue", "1B2 1111", Country.USA);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("Mazda MX-5", 1.8, CarType.CABRIO, "red", "6T4 2526", Country.USA);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            c = new Car("VW Golf", 1.6, CarType.HATCHBACK, "yellow", "2B2 4946", Country.GERMANY);
            carNumbers.add(c.getNumberPlate());
            cars.put(CarManager.encode(c.getNumberPlate()), c);
            // insert a list of cars' number plates
            cars.put(CarManager.CAR_NUMBERS_KEY, carNumbers);
        } catch (Exception e) {
            log.warning("An exception occured while populating the database!");
        }

        log.info("Successfully imported data!");
    }

    private BeanManager getBeanManager() {
        // BeanManager is only available when CDI is available. This is achieved by the presence of beans.xml file
        InitialContext context;
        Object result;
        try {
            context = new InitialContext();
            result = context.lookup("java:comp/BeanManager"); // lookup in JBossAS
        } catch (NamingException e) {
            throw new RuntimeException("BeanManager could not be found in JNDI", e);
        }
        return (BeanManager) result;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextualInstance(final BeanManager manager, final Class<T> type) {
        T result = null;
        Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type));
        if (bean != null) {
            CreationalContext<T> context = manager.createCreationalContext(bean);
            if (context != null) {
                result = (T) manager.getReference(bean, type, context);
            }
        }
        return result;
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof Application;
    }
}