package com.github.scrud.util;

/**
 * An annotation that a class is able to participate in a micro-test (fast unit test w/ maximum real code) without being awkward.
 * It is awkward if it is slow, uses the network or filesystem, or behaves inconsistently.
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/29/13
 *         Time: 5:34 PM
 */
public @interface MicrotestCompatible {
  String use() default "self";
}
