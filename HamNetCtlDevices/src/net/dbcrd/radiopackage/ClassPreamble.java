/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.radiopackage;
import java.lang.annotation.*;
/**
 *
 * @author dbcurtis
 */
@Documented
public @interface ClassPreamble {
    /**
     *
     * @return  a String with the author info
     */
    String author() default "Dan Curtis";
    /**
     *
     * @return  a String with the date info
     */
    String date();
    /**
     *
     * @return a String with the current revision info
     */
    int currentRevision() default 1;
    /**
     *
     * @return a String with the last modified date info
     */
    String lastModified() default "N/A";
    /**
     *
     * @return a String with the last modified by info
     */
    String lastModifiedBy() default "N/A";
    /**
     *
     * @return a String with the copyright info
     */
    String copyright() default "(C) 2009-2010, Daniel B. Curtis, all rights reserved.";

}
