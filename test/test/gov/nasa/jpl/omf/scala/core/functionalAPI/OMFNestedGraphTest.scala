/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * *   Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *   Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * *   Neither the name of Caltech nor its operating division, the Jet
 *    Propulsion Laboratory, nor the names of its contributors may be
 *    used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package test.gov.nasa.jpl.omf.scala.core.functionalAPI

import gov.nasa.jpl.omf.scala.core._
import gov.nasa.jpl.omf.scala.core.RelationshipCharacteristics._
import gov.nasa.jpl.omf.scala.core.TerminologyKind._

import scala.language.{implicitConversions, postfixOps}
import org.scalatest._, exceptions._
import scala.{StringContext,Unit}
import scala.util.control.Exception._
import scalaz._, Scalaz._
import scala.collection.immutable.{List,Set}

abstract class OMFNestedGraphTest[omf <: OMF]
( val saveStore: omf#Store, saveOps: OMFOps[omf],
  val loadStore: omf#Store, loadOps: OMFOps[omf]
) extends WordSpec with Matchers {

  def preOMFSave(): Unit

  def postOMFSave(): Unit

  def withOMFSave
  (testCode: (omf#Store, OMFOps[omf]) => Set[java.lang.Throwable] \/ Unit)
  : Unit =

    nonFatalCatch[Unit]
      .withApply {
        (cause: java.lang.Throwable) =>
          throw new TestFailedException(
            message=s"withOMFSave failed: ${cause.getMessage}",
            cause=cause,
            failedCodeStackDepth=2)
      }
      .apply({
        preOMFSave()
        val result = testCode(saveStore, saveOps)
        postOMFSave()
        result.isRight should equal(true)
      })


  def preOMFLoad(): Unit

  def postOMFLoad(): Unit

  def withOMFLoad
  (testCode: (omf#Store, OMFOps[omf]) => Set[java.lang.Throwable] \/ Unit)
  : Unit =

    nonFatalCatch[Unit]
      .withApply {
        (cause: java.lang.Throwable) =>
          throw new TestFailedException(
            message=s"withOMFLoad failed: ${cause.getMessage}",
            cause=cause,
            failedCodeStackDepth=2)
      }
      .apply({
        preOMFLoad()
        val result = testCode(loadStore, loadOps)
        postOMFLoad()
        result.isRight should equal(true)
      })

  "nested graph roundtrip test" when {

    "construct nested tboxes and save them" in withOMFSave { (s, o) =>

      implicit val store = s
      implicit val ops = o
      import ops._

      for {
        xsd_iri <- makeIRI("http://www.w3.org/2001/XMLSchema")
        xsd <- loadTerminologyGraph(xsd_iri)
        int_iri <- makeIRI("http://www.w3.org/2001/XMLSchema#integer")
        integer = lookupScalarDataType(xsd._1, int_iri, recursively = false)
        string_iri <- makeIRI("http://www.w3.org/2001/XMLSchema#string")
        string = lookupScalarDataType(xsd._1, string_iri, recursively = false)

        base_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/base/base")
        base <- makeTerminologyGraph(base_iri, isDefinition)
        base_extends_xsd <- addTerminologyGraphExtension(base, xsd._1)
        identifiedElement <- addEntityAspect(base, "IdentifiedElement")
        hasIdentifier = addDataRelationshipFromEntityToScalar(
          graph = base,
          source = identifiedElement,
          target = string.get,
          dataRelationshipName = "hasIdentifier")
        mission_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/mission/mission")
        mission <- makeTerminologyGraph(mission_iri, isDefinition)
        mission_extends_base <- addTerminologyGraphExtension(mission, base)
        component <- addEntityConcept(mission, "Component", isAbstract = false)
        component_extends_identifiedElement <- addEntityDefinitionAspectSubClassAxiom(
          graph = mission,
          sub = component,
          sup = identifiedElement)
        function <- addEntityConcept(mission, "Function", isAbstract = false)
        function_extends_identifiedElement <- addEntityDefinitionAspectSubClassAxiom(
          graph = mission,
          sub = function,
          sup = identifiedElement)
        component_performs_function <- addEntityReifiedRelationship(
          graph = mission,
          source = component,
          target = function,
          characteristics = List(isAsymmetric, isIrreflexive, isInverseFunctional),
          reifiedRelationshipName = "Performs",
          unreifiedRelationshipName = "performs",
          unreifiedInverseRelationshipName = "isPerformedBy".some,
          isAbstract = false)
        project_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/project/project")
        project <- makeTerminologyGraph(project_iri, isDefinition)
        project_extends_mission <- addTerminologyGraphExtension(project, mission)
        workPackage <- addEntityConcept(project, "WorkPackage", isAbstract = false)

        g_iri <- makeIRI("http://example.org/G")
        g <- makeTerminologyGraph(g_iri, isDefinition)
        g_extends_project <- addTerminologyGraphExtension(g, project)

        g_A <- addEntityConcept(g, "A", isAbstract=false)
        g_A_isa_component <- addEntityConceptSubClassAxiom(g, g_A, component)

        g_B <- addEntityConcept(g, "B", isAbstract=false)
        g_B_isa_component <- addEntityConceptSubClassAxiom(g, g_B, component)

        g_C <- addEntityConcept(g, "C", isAbstract=false)
        g_C_isa_function <- addEntityConceptSubClassAxiom(g, g_A, component)

        p1_iri <- makeIRI("http://example.org/P1")
        p1 <- makeTerminologyGraph(p1_iri, isDefinition)
        g_authorizes_p1 <- addEntityConcept(g, "P1", isAbstract=false)
        g_authorizes_p1_WP <- addEntityConceptSubClassAxiom(g, g_authorizes_p1, workPackage)
        g_nests_p1 <- addNestedTerminologyGraph(nestingParent=g, nestingContext=g_authorizes_p1, nestedChild=p1)

        p1_asserts_A_performs_C <- addEntityReifiedRelationship(
          graph = p1,
          source = g_A,
          target = g_C,
          characteristics = List(isAsymmetric, isIrreflexive, isInverseFunctional),
          reifiedRelationshipName = "Performs",
          unreifiedRelationshipName = "performs",
          unreifiedInverseRelationshipName = "isPerformedBy".some,
          isAbstract = false)
        p1_asserts_A_performsFunction_C <-
        addEntityReifiedRelationshipSubClassAxiom(graph=p1, sub=p1_asserts_A_performs_C, sup=component_performs_function)

        p2_iri <- makeIRI("http://example.org/P2")
        p2 <- makeTerminologyGraph(p2_iri, isDefinition)
        g_authorizes_p2 <- addEntityConcept(g, "P2", isAbstract=false)
        g_authorizes_p2_WP <- addEntityConceptSubClassAxiom(g, g_authorizes_p2, workPackage)
        g_nests_p2 <- addNestedTerminologyGraph(nestingParent=g, nestingContext=g_authorizes_p2, nestedChild=p2)

        p2_asserts_B_performs_C <- addEntityReifiedRelationship(
          graph = p2,
          source = g_B,
          target = g_C,
          characteristics = List(isAsymmetric, isIrreflexive, isInverseFunctional),
          reifiedRelationshipName = "Performs",
          unreifiedRelationshipName = "performs",
          unreifiedInverseRelationshipName = "isPerformedBy".some,
          isAbstract = false)
        p2_asserts_B_performsFunction_C <-
        addEntityReifiedRelationshipSubClassAxiom(graph=p2, sub=p2_asserts_B_performs_C, sup=component_performs_function)

      } yield {
        lookupNestingAxiomForNestedChildIfAny(nestedG = g).isEmpty should be(true)
        lookupNestingAxiomForNestedChildIfAny(nestedG = p1).contains(g_nests_p1) should be(true)
        lookupNestingAxiomForNestedChildIfAny(nestedG = p2).contains(g_nests_p2) should be(true)

        lookupNestingAxiomForNestingContextIfAny(nestingC = component).isEmpty should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = function).isEmpty should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = g_authorizes_p1).contains(g_nests_p1) should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = g_authorizes_p2).contains(g_nests_p2) should be(true)

        lookupNestingAxiomsForNestingParent(nestingG = p1).isEmpty should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = p1).isEmpty should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = g).contains(g_authorizes_p1) should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = g).contains(g_authorizes_p2) should be(true)
      }

    }

    "read tboxes and check them" in withOMFLoad { (s, o) =>

      implicit val store = s
      implicit val ops = o
      import ops._

      for {
        xsd_iri <- makeIRI("http://www.w3.org/2001/XMLSchema")
        xsd <- loadTerminologyGraph(xsd_iri)
        int_iri <- makeIRI("http://www.w3.org/2001/XMLSchema#integer")
        integer = lookupScalarDataType(xsd._1, int_iri, recursively = false)
        string_iri <- makeIRI("http://www.w3.org/2001/XMLSchema#string")
        base_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/base/base")
        base <- loadTerminologyGraph(base_iri)
        mission_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/mission/mission")
        mission <- loadTerminologyGraph(mission_iri)
        component_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/mission/mission#Component")
        component_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/mission/mission#Component")
        component_performs_function_iri <- makeIRI("http://imce.jpl.nasa.gov/foundation/mission/mission#Performs")
      } yield {
        lookupNestingAxiomForNestedChildIfAny(nestedG = g).isEmpty should be(true)
        lookupNestingAxiomForNestedChildIfAny(nestedG = p1).contains(g_nests_p1) should be(true)
        lookupNestingAxiomForNestedChildIfAny(nestedG = p2).contains(g_nests_p2) should be(true)

        lookupNestingAxiomForNestingContextIfAny(nestingC = component).isEmpty should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = function).isEmpty should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = g_authorizes_p1).contains(g_nests_p1) should be(true)
        lookupNestingAxiomForNestingContextIfAny(nestingC = g_authorizes_p2).contains(g_nests_p2) should be(true)

        lookupNestingAxiomsForNestingParent(nestingG = p1).isEmpty should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = p1).isEmpty should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = g).contains(g_authorizes_p1) should be(true)
        lookupNestingAxiomsForNestingParent(nestingG = g).contains(g_authorizes_p2) should be(true)
      }

    }
  }
}