/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.testing

import androidx.room.compiler.processing.XProcessingStep
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.util.XTestInvocation

/**
 * Turns a database processing step to an invocation handler where it will be automatically invoked
 * as if it is running as part of a processor.
 */
fun XProcessingStep.asTestInvocationHandler(
    delegate: (XTestInvocation) -> Unit
): (XTestInvocation) -> Unit = { invocation ->
    val elementsByAnnotation =
        annotations().associateWith {
            invocation.roundEnv
                .getElementsAnnotatedWith(it)
                .filterIsInstance<XTypeElement>()
                .toSet()
        }
    this.process(env = invocation.processingEnv, elementsByAnnotation = elementsByAnnotation, false)
    delegate(invocation)
}
