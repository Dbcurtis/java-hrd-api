

package net.dbcrd.radiopackage.omnirigserial;
import java.lang.annotation.*;
/**
 *
 * @author dbcurtis
 */
@Documented
public @interface ClassPreamble {
    /**
     *
     * @return
     */
    String author() default "Dan Curtis";
    /**
     *
     * @return
     */
    String date();
    /**
     *
     * @return
     */
    int currentRevision() default 1;
    /**
     *
     * @return
     */
    String lastModified() default "N/A";
    /**
     *
     * @return
     */
    String lastModifiedBy() default "N/A";
    /**
     *
     * @return
     */
    String copyright() default "(C) 2009-2010, Daniel B. Curtis, all rights reserved.";

}
