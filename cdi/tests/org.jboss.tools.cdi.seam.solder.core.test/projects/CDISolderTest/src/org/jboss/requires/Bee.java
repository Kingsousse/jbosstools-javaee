package org.jboss.requires;

import javax.inject.Named;

import org.jboss.solder.core.Requires;

@Requires({"org.jboss.requires.Beehive", "org.jboss.requires.Flower"})
@Named("bee")
public class Bee {

}
