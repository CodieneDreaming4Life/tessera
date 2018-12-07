package com.quorum.tessera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ServiceLoaderUtil {

    static <T> Optional<T> load(Class<T> type) {
        Iterator<T> it = ServiceLoader.load(type).iterator();
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }

    /*
    * //WORKAROUND
    * TODO: harden up service loader this function shouldn't be required. 
     * Ih there are more than 1 instances return this first one.  
     */
    static <T> Optional<T> load(Class<T> type, Class prefered) {
        Iterator<T> it = ServiceLoader.load(type).iterator();

        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            T o = it.next();
            if(prefered.isInstance(o)) {
                return Optional.of(o);
            }
            
            all.add(o);
        }

        return all.stream().findAny();
        
    }

}
