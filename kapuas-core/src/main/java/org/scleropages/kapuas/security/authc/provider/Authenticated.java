package org.scleropages.kapuas.security.authc.provider;

import org.scleropages.kapuas.security.AuthenticationDetails;

import java.io.Serializable;
import java.util.Date;

/**
 * defined client's information that already authenticated by security provider.
 */
public interface Authenticated extends Serializable {

    /**
     * return client principal
     *
     * @return
     */
    Object principal();

    /**
     * return client remote host
     *
     * @return
     */
    String host();

    /**
     * return authenticated time.
     *
     * @return
     */
    Date time();

    /**
     * return client details information
     *
     * @return
     */
    AuthenticationDetails details();

    /**
     * return authentication provider identifier
     *
     * @return
     */
    Serializable realm();
}