package net.jahhan.registry;

import java.util.List;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;

import net.jahhan.common.extension.annotation.Extension;

@Extension("none")
@Singleton
public class NoneRegistryFactory extends AbstractRegistryFactory {

    public Registry createRegistry(URL url) {
        return new Registry(){

			@Override
			public URL getUrl() {
				return url;
			}

			@Override
			public boolean isAvailable() {
				return false;
			}

			@Override
			public void destroy() {
				
			}

			@Override
			public void register(URL url) {
				
			}

			@Override
			public void unregister(URL url) {
				
			}

			@Override
			public void subscribe(URL url, NotifyListener listener) {
				
			}

			@Override
			public void unsubscribe(URL url, NotifyListener listener) {
				
			}

			@Override
			public List<URL> lookup(URL url) {
				return null;
			}
        	
        };
    }

}