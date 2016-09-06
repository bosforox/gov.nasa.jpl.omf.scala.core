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

package gov.nasa.jpl.omf.scala

import scala.{Boolean, Option}
import scala.collection.immutable._

package object core {

  def getImportedTerminologyGraphs[Omf <: OMF]
  ( tbox: Omf#ModelTerminologyGraph,
    onlyCompatibleKind: Boolean = true  )
  ( implicit ops: OMFOps[Omf], store: Omf#Store )
  : Set[Omf#ModelTerminologyGraph] = {

    val s = ops.fromTerminologyGraph(tbox)

    def hasCompatibleKind(g: Omf#ModelTerminologyGraph)
    : Boolean
    = !onlyCompatibleKind || TerminologyKind.compatibleKind(s.kind)(ops.getTerminologyGraphKind(g))

    val i1
    : Option[Omf#ModelTerminologyGraph]
    = ops.lookupNestingAxiomForNestedChildIfAny(tbox).map(ops.getNestingParentGraphOfAxiom).filter(hasCompatibleKind)

    val i2
    : Iterable[Omf#ModelTerminologyGraph]
    = s.imports.filter(hasCompatibleKind)

    (i1 ++ i2).toSet
  }

  /**
   * The reflexive transitive closure of terminology graphs reachable by following
   * TerminologyGraphDirectImportAxiom (from importing to imported) and
   * TerminologyGraphDirectNestingParentAxiom (from nested child to nesting parent).
   *
   * @param g The terminology graph whose direct & indirect imports and nesting parents are included in the result
   *          (subject to kind filtering)
   * @param onlyCompatibleKind determines the filtering for imported & nesting parent terminology graphs
   * @return gs:
   *         if onlyCompatibleKind is true; then gs contains g and all directly or indirectly
   *         imported / nesting parents g' where g has compatible kind with g'
   *         if onlyCompatibleKind is false; then gs contains g and all directly or indirectly
   *         imported / nesting parents g' regardless of whether g is compatible with g'
   *
   * If:
   * TerminologyGraphDirectImportAxiom(importing=G1, imported=G2)
   * TerminologyGraphDirectImportAxiom(importing=G2, imported=G3)
   * Then:
   * G1 imports G2,G3
   * G2 imports G3
   *
   * If:
   * TerminologyGraphDirectImportAxiom(importing=G1, imported=G2)
   * TerminologyGraphDirectNestingParentAxiom(nestedChild=G2, nestingParent=G3)
   * TerminologyGraphDirectImportAxiom(importing=G3, imported=G4)
   * Then:
   * G1 imports G2,G3,G4
   * G3 imports G4
   *
   * If:
   * TerminologyGraphDirectImportAxiom(importing=G1, imported=G2a)
   * TerminologyGraphDirectNestingParentAxiom(nestedChild=G2a, nestingParent=G3)
   * TerminologyGraphDirectNestingParentAxiom(nestedChild=G2b, nestingParent=G3)
   * TerminologyGraphDirectImportAxiom(importing=G3, imported=G4)
   * Then:
   * G1 imports G2a,G3,G4
   * G3 imports G4
   */
  def terminologyGraphImportClosure[Omf <: OMF, TG <: Omf#ModelTerminologyGraph]
  ( g: TG,
    onlyCompatibleKind: Boolean = true )
  ( implicit ops: OMFOps[Omf], store: Omf#Store )
  : Set[Omf#ModelTerminologyGraph] = {

    def step
    (gi: Omf#ModelTerminologyGraph)
    : Set[Omf#ModelTerminologyGraph]
    = getImportedTerminologyGraphs(gi, onlyCompatibleKind)

    val result
    : Set[Omf#ModelTerminologyGraph]
    = OMFOps.closure[Omf#ModelTerminologyGraph, Omf#ModelTerminologyGraph](g, step) + g

    result
  }

  /**
   * Aggregates all entities defined in terminology graphs
   * @param tboxes: a set of terminology graphs
   * @return a 3-tuple of the aspects, concepts and relationships entities defined in the graphs:
   */
  def allEntities[Omf <: OMF]
  ( tboxes: Set[Omf#ModelTerminologyGraph] )
  ( implicit ops: OMFOps[Omf], store: Omf#Store )
  : ( Set[Omf#ModelEntityAspect],
    Set[Omf#ModelEntityConcept],
    Set[Omf#ModelEntityReifiedRelationship] ) = {

    import ops._

    val entities0 =
      ( Set[Omf#ModelEntityAspect](), Set[Omf#ModelEntityConcept](), Set[Omf#ModelEntityReifiedRelationship]() )
    val entitiesN =
      ( entities0 /: ( for { tbox <- tboxes } yield {
        val s =
          fromTerminologyGraph( tbox )
        ( s.aspects.toSet, s.concepts.toSet, s.reifiedRelationships.toSet )
      } ) ) { case ( ( ai, ci, ri ), ( aj, cj, rj ) ) => ( ai ++ aj, ci ++ cj, ri ++ rj ) }

    entitiesN
  }

}