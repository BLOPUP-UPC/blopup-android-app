package edu.upc.sdk.utilities

import edu.upc.sdk.library.models.Visit
import rx.Observable

fun <T> Observable<T>.execute(): T = this.single().toBlocking().first()
