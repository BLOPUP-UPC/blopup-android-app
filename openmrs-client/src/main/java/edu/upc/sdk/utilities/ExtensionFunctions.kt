package edu.upc.sdk.utilities

import rx.Observable

fun <T> Observable<T>.execute(): T = this.single().toBlocking().first()
