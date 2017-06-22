/*
 * Copyright 2015 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * License Terms
 */

package gov.nasa.jpl.omf.scala.core

import java.util.UUID

import gov.nasa.jpl.imce.oml.tables.{AnnotationEntry, AnnotationProperty, LocalName}

import scala.collection.immutable.Set

trait ModuleSignature[omf <: OMF] {
  val uuid: UUID
  val name: LocalName
  /**
    * the identity of the terminology as a container for several descriptions and as the context
    * for extending other terminologies
    */
  val iri: omf#IRI
  val annotationProperties: scala.collection.Iterable[AnnotationProperty]

  val annotations: scala.collection.Iterable[(AnnotationProperty, scala.collection.immutable.Set[AnnotationEntry])]

  def importedTerminologies
  (implicit ops: OMFOps[omf])
  : Set[omf#TerminologyBox]

  def importedDescriptions
  (implicit ops: OMFOps[omf])
  : Set[omf#DescriptionBox]
}