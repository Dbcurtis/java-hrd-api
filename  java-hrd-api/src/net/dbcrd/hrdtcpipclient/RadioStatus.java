
package net.dbcrd.hrdtcpipclient;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * A subclass of properties that contains any radio information of interest.
 *
 * @author Daniel B. Curtis N6WN
 *
 *
 * Copyright 2010 Daniel B. Curtis
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
public class RadioStatus extends Properties{
    private static final long serialVersionUID = 128282828282L;
    private static final Logger THE_LOGGER=Logger.getLogger(RadioStatus.class.getName());

   RadioStatus(){
       super();
   }

/**
 * Get a clone of the RadioStatus.
 * @return a RadioStatus that is a clone of the current status.
 */
   public RadioStatus getProperties (){
       return (RadioStatus)super.clone();
   }
/**
 * Returns true ** not implemented yet.
 * @return a boolean generated from the properties to indicate that the radio is operational. 
 */
   public boolean isValid(){
       THE_LOGGER.warning("******************not implemented");
       return true;
   }
}
