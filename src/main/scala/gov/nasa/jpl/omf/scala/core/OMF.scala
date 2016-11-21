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

import gov.nasa.jpl.imce.omf.schema.tables.LocalName

import scala.{Boolean, Option}
import scala.collection.immutable.{Iterable, Map}
import gov.nasa.jpl.omf.scala.core.RelationshipCharacteristics._
import gov.nasa.jpl.omf.scala.core.TerminologyKind._

/**
  * OMF is JPL's Ontological Modeling Framework, a pure functional interface specification
  * supporting a rigorous integratation of two widely used paradigms:
  * - modeling a system in the sense of the Object Management Group (OMG) modeling specifications
  * (e.g. the Unified Modeling Language (UML) and the Systems Modeling Language (SysML))
  * - ontologies for describing a system according to a analyzable vocabulary for creating and reasoning
  * about such descriptions in the sense of the World-Wide Web (W3C) Ontology Web Language (OWL) standard.
  *
  * The OMF interface specification involves 4 main components:
  * - OMFStore for the management of OMF models
  * - OMFIRI for identifying "things"
  * - OMFtbox for describing a conceptual model of a domain
  * - OMFabox for describing particular situations in that domain using the OMFtbox vocabulary for that domain
  */
trait OMF extends OMFstore with OMFiri with OMFtbox with OMFabox {

}

trait OMFstore {
  /**
    * The type of an OMF storage provider.
    *
    * An instance of a Store is an implicit parameter for the construction-related operations in OMFDSL.
    */
  type Store

}

/**
  * The types involved in the identification of "things" in OMF
  */
trait OMFiri {

  /**
    * An IRI identifies "things" in OMFtbox or OMFabox graphs.
    * In an OMFtbox graph, an IRI identifies ModelTerminologyGraph, ModelEntityDefinitions and ModelDataTypeDefinitions.
    * In an OMFabox graph, an IRI identifies ModelInstanceGraphs and ModelNamedIndividuals
    */
  type IRI
}

/**
  * Types for defining OMF tbox graphs specifying conceptual models of domains.
  */
trait OMFtbox {

  /**
    * In OMF, the specification of the conceptual model of a domain is defined in a TBox graph.
    */
  type ModelTerminologyGraph
  type ImmutableModelTerminologyGraph <: ModelTerminologyGraph
  type MutableModelTerminologyGraph <: ModelTerminologyGraph
  type Mutable2ImmutableTerminologyMap <: Map[MutableModelTerminologyGraph, ImmutableModelTerminologyGraph]

  /**
    * A ModelTypeTerm is the basic unit for defining the conceptual model of a domain in an OMF ModelTerminologyGraph.
    *
    * There are 4 kinds of ModelTypeTerms:
    *
    * - ModelEntityDefinition:
    * the vocabulary for the conceptual modeling of a domain in terms of "things" that have identity semantics
    *
    * - ModelDataTypeDefinition:
    * the vocabulary for the conceptual modeling of a domain in terms of data that has value semantics
    *
    * - ModelDataRelationship:
    * binary, directed relationships whose domain is either an entity or datatype and whose range is a datatype
    *
    * - ModelEntityUnreifiedRelationship:
    * binary, directed relationships whose domain & range are entities and whose identity is the related objects
    */
  type ModelTypeTerm

  type ModelEntityUnreifiedRelationship <: ModelTypeTerm

  /**
    * A ModelEntityDefinition defines the vocabulary for the conceptual modeling
    * of a domain in terms of "things" that have an intrinsic identity semantics.
    *
    * - ModelEntityAspect
    * - ModelEntityConcept
    * - ModelEntityReifiedRelationship
    */
  type ModelEntityDefinition <: ModelTypeTerm

  /**
    * A ModelEntityAspect defines an abstraction that can be the superclass of other
    * ModelEntityAspects, ModelEntityConcepts or ModelEntityReifiedRelationships.
    *
    * In an OMF ABox graph, a ModelEntityAspect cannot be explicitly instantiated;
    * however, a ModelEntityConcept or ModelEntityReifiedRelationship specialization
    * of a ModelEntityAspect can be explicitly instantiated.
    */
  type ModelEntityAspect <: ModelEntityDefinition

  /**
    * A ModelEntityConcept defines a concept in the conceptual modeling of a domain.
    *
    * In an OMF ABox, each instance of a ModelEntityConcept has a unique identity.
    */
  type ModelEntityConcept <: ModelEntityDefinition

  /**
    * A ModelEntityReifiedRelationship defines a binary, directed relationship in the conceptual modeling of a domain.
    *
    * The relationship domain (aka source) and range (aka target) can be any kind of ModelEntityDefinition
    */
  type ModelEntityReifiedRelationship <: ModelEntityDefinition

  /**
    * A ModelDataTypeDefinition defines the vocabulary for the conceptual modeling
    * of a domain in terms of data that has value semantics (i.e, equality).
    *
    * There are 2 kinds of ModelDataTypeDefinitions:
    * - ModelScalarDataType, an atomic datatype in the sense of XML Schema 1.1 DataTypes
    * - ModelStructuredDataType, a structured datatype with data property relationships to other ModelDataTypeDefinitions
    */
  type ModelDataTypeDefinition <: ModelTypeTerm

  /**
    * A ModelScalarDataType defines a scalar datatype in a conceptual model
    * in the sense that a scala datatype is 'atomic' in the sense of XML Schema 1.1 DataTypes.
    *
    * A value of a scalar datatype is always represented according to its lexical representation as a string.
    * The value semantics of a ModelScalarDataType follows XML Schema 1.1 DataTypes, that is, it is simply
    * the equality of the lexical representation of a value of a ModelScalarDataType.
    *
    * @see http://www.w3.org/TR/xmlschema11-2/#anyAtomicType
    */
  type ModelScalarDataType <: ModelDataTypeDefinition

  /**
    * A ModelStructuredDataType defines a structured datatype in a conceptual model
    * in the sense that a structured datatype is defined only in terms of ModelDataRelationships
    * to other ModelDataTypeDefinitions and that the value semantics of a structured datatype
    * is based on the equality of the value of its ModelDataRelationships.
    */
  type ModelStructuredDataType <: ModelDataTypeDefinition

  /**
    * A ModelDataRelationship is an abstraction for a 2x2 matrix for
    * binary directed relationships whose range is a ModelDataTypeDefinition.
    *
    * One axis is the domain of the relationship:
    * - ModelDataRelationshipFromEntity
    * - ModelDataRelationshipFromStructure
    *
    * Another axis is the range of the relationship:
    * - ModelDataRelationshipToScalar
    * - ModelDataRelationshipToStructure
    */
  type ModelDataRelationship <: ModelTypeTerm

  /**
    * A ModelDataRelationshipFrom is the abstraction of the domain of a ModelDataRelationship:
    * - ModelDataRelationshipFromEntity
    * - ModelDataRelationshipFromStructure
    */
  type ModelDataRelationshipFrom
  type ModelDataRelationshipFromEntity <: ModelDataRelationshipFrom
  type ModelDataRelationshipFromStructure <: ModelDataRelationshipFrom

  /**
    * A ModelDataRelationshipFrom is the abstraction of the range of a ModelDataRelationship:
    * - ModelDataRelationshipToScalar
    * - ModelDataRelationshipToStructure
    */
  type ModelDataRelationshipTo
  type ModelDataRelationshipToScalar <: ModelDataRelationshipTo
  type ModelDataRelationshipToStructure <: ModelDataRelationshipTo

  /**
    * A ModelDataRelationshipFromEntityToScalar is a ModelDataRelationship
    * with a domain that is a ModelDataRelationshipFromEntity
    * and with a range that is a ModelDataRelationshipToScalar
    */
  type ModelDataRelationshipFromEntityToScalar <: ModelDataRelationship
    with ModelDataRelationshipFromEntity
    with ModelDataRelationshipToScalar

  /**
    * A ModelDataRelationshipFromEntityToStructure is a ModelDataRelationship
    * with a domain that is a ModelDataRelationshipFromEntity
    * and with a range that is a ModelDataRelationshipToStructure
    */
  type ModelDataRelationshipFromEntityToStructure <: ModelDataRelationship
    with ModelDataRelationshipFromEntity
    with ModelDataRelationshipToStructure

  /**
    * A ModelDataRelationshipFromStructureToScalar is a ModelDataRelationship
    * with a domain that is a ModelDataRelationshipFromStructure
    * and with a range that is a ModelDataRelationshipToScalar
    */
  type ModelDataRelationshipFromStructureToScalar <: ModelDataRelationship
    with ModelDataRelationshipFromStructure
    with ModelDataRelationshipToScalar

  /**
    * A ModelDataRelationshipFromStructureToStructure is a ModelDataRelationship
    * with a domain that is a ModelDataRelationshipFromStructure
    * and with a range that is a ModelDataRelationshipToStructure
    */
  type ModelDataRelationshipFromStructureToStructure <: ModelDataRelationship
    with ModelDataRelationshipFromStructure
    with ModelDataRelationshipToStructure

  /**
    * A ModelTermAxiom is the abstraction for statements about
    * ModelTypeTerms in an ModelTerminologyGraph
    */
  type ModelTermAxiom

  /**
    * A ModelScalarDataRelationshipRestrictionAxiomFromEntityToLiteral is a ModelTermAxiom assertion
    * about a ModelEntityDefinition that is in the domain of a ModelDataRelationshipFromEntityToScalar
    * with a range restricted to be exactly a singleton set of one literal value.
    */
  type ModelScalarDataRelationshipRestrictionAxiomFromEntityToLiteral <: ModelTermAxiom

  /**
    * An EntityDefinitionAspectSubClassAxiom is a ModelTermAxion assertion
    * about a ModelEntityDefinition as a subclass for a superclass ModelEntityAspect
    */
  type EntityDefinitionAspectSubClassAxiom <: ModelTermAxiom

  /**
    * An EntityConceptDesignationTerminologyGraphAxiom is a ModelTermAxion assertion
    * about a ModelEntityConcept whose complete concept designation
    * is specified in a designation ModelTerminologyGraph.
    */
  type EntityConceptDesignationTerminologyGraphAxiom <: ModelTermAxiom

  /**
    * An EntityConceptSubClassAxiom is a ModelTermAxiom assertion about
    * a subclass/superclass relationship between two ModelEntityConcepts
    */
  type EntityConceptSubClassAxiom <: ModelTermAxiom

  /**
    * An EntityDefinitionRestrictionAxiom is a ModelTermAxiom assertion about
    * constraining a ModelEntityReifiedRelationship
    * for a sub-domain ModelEntityDefinition
    * to a restricted sub-range ModelEntityDefinition.
    *
    * This restriction constraint can be universal or existential.
    */
  type EntityDefinitionRestrictionAxiom <: ModelTermAxiom

  /**
    * An EntityDefinitionUniversalRestrictionAxiom is a ModelTermAxiom assertion about
    * constraining a ModelEntityReifiedRelationship
    * for objects of a sub-domain ModelEntityDefinition
    * to be related to only a restricted sub-range ModelEntityDefinition.
    */
  type EntityDefinitionUniversalRestrictionAxiom <: EntityDefinitionRestrictionAxiom

  /**
    * An EntityDefinitionUniversalRestrictionAxiom is a ModelTermAxiom assertion about
    * constraining a ModelEntityReifiedRelationship
    * for objects of a sub-domain ModelEntityDefinition
    * to be related to include a sub-range ModelEntityDefinition.
    */
  type EntityDefinitionExistentialRestrictionAxiom <: EntityDefinitionRestrictionAxiom

  /**
    * An EntityReifiedRelationshipSubClassAxiom is a ModelTermAxiom assertion about
    * a subclass/superclass relationship between two ModelEntityReifiedRelationships
    */
  type EntityReifiedRelationshipSubClassAxiom <: ModelTermAxiom

  /**
    * A ScalarDataTypeFacetRestrictionAxiom is a ModelTermAxiom assertion about
    * a subtype/supertype relationship between two ModelScalarDataTypes according
    * to one or more XML Schema 1.1 DataType facet restrictions:
    * the value space of the subtype ModelScalarDataType is the subset of the supertype ModelScalarDataType
    * such that a value from the value space of the supertype is in the value space of the subtype
    * if and only if the value satisfies all the facet restrictions.
    *
    * @see http://www.w3.org/TR/xmlschema11-2/#sec-datatypes-and-facets
    * @see http://www.w3.org/TR/owl2-syntax/#Datatype_Restrictions
    */
  type ScalarDataTypeFacetRestrictionAxiom <: ModelTermAxiom

  /**
    * A TerminologyGraphAxiom is the abstraction for statements about
    * ModelTerminologyGraphs
    */
  type TerminologyGraphAxiom

  /**
    * A TerminologyGraphDirectExtensionAxiom(extendingChild=G1, extendedParent=G1)
    * is a TerminologyGraphAxiom assertion about the vocabulary of an extending ModelTerminologyGraph G1
    * that directly extends the vocabulary of an extended ModelTerminologyGraph G2 in the sense
    * that vocabulary terms defined in G1 can be defined in terms of or as specializations or restrictions of
    * vocabulary terms defined in G2 or in another graph G3 that is directly or indirectly an extended parent of G2.
    *
    * If:
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G1, extendedParent=G2)
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G2, extendedParent=G3)
    * Then:
    * G1 extends G2,G3
    * G2 extends G3
    *
    * If:
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G1, extendedParent=G2)
    * TerminologyGraphDirectNestingParentAxiom(extendingChild=G2, nestingParent=G3)
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G3, extendedParent=G4)
    * Then:
    * G1 extends G2,G3,G4
    * G3 extends G4
    *
    * If:
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G1, extendedParent=G2a)
    * TerminologyGraphDirectNestingParentAxiom(nestedChild=G2a, nestingParent=G3)
    * TerminologyGraphDirectNestingParentAxiom(nestedChild=G2b, nestingParent=G3)
    * TerminologyGraphDirectExtensionAxiom(extendingChild=G3, extendedParent=G4)
    * Then:
    * G1 extends G2a,G3,G4
    * G3 extends G4
    */
  type TerminologyGraphDirectExtensionAxiom <: TerminologyGraphAxiom

  /**
    * A TerminologyGraphDirectNestingAxiom(nestingParent=G1, nestingContext=C, nestedChild=G2)
    * is a TerminologyGraphAxiom assertion about a ModelTerminologyGraph G1 that is the
    * authorization context for a ModelEntityConcept C defined in a ModelTerminologyGraph G2
    * that is also the extended parent of G1.
    *
    * Invariants:
    * fromTerminologyGraph(G1).concepts.contains(C)
    * lookupNestingAxiomsForNestingParent(G1).contains(this)
    * lookupNestingAxiomForNestedChildIfAny(G2).contains(this)
    *
    * If:
    * TerminologyGraphDirectNestingAxiom(nestedChild=G1, nestingParent=G2)
    * TerminologyGraphDirectNestingAxiom(nestedChild=G2, nestingParent=G3)
    * Then:
    * G1 has nesting parents G2,G3
    * G2 has nesting parents G3
    * G2 has nested children G1
    * G3 has nested children G1,G2
    */
  type TerminologyGraphDirectNestingAxiom <: TerminologyGraphAxiom
}

/**
  * Types for defining OMF abox graphs for describing domain-specific situations according
  * to the OMF tbox graphs defining the conceptual models of these domains.
  */
trait OMFabox {

  /**
    * In OMF, the description of a particular domain-specific situation is defined in an ABox graph
    * for the OMF TBox graph of that domain.
    */
  type ModelInstanceGraph
  type ImmutableModelInstanceGraph <: ModelInstanceGraph
  type MutableModelInstanceGraph <: ModelInstanceGraph

  /**
    * A ModelInstanceAssertion is the basic unit for describing a domain-specific situation in an OMF ABox graph.
    *
    * There are
    * - ModelInstanceRelationshipAssertion:
    * Asserts an ModelEntityUnreifiedRelationship between ModelNamedIndividuals
    *
    * - ModelNamedIndividual:
    * Identifies an instance of a ModelEntityConcept, ModelEntityReifiedRelationship,
    * ModelStructuredDataType, ModelInstanceDataRelationshipFromEntityToStructure,
    * ModelInstanceDataRelationshipFromStructureToStructure
    *
    * - ModelInstanceDataRelationshipFromEntityToScalar:
    * Asserts the scalar value of a data property relationship for a ModelEntityInstance
    *
    * - ModelInstanceDataRelationshipFromStructureToScalar:
    * Asserts the scalar value of a data property relationship for a ModelStructuredDataType
    */
  type ModelInstanceAssertion

  /**
    * A ModelInstanceRelationshipAssertion asserts an ModelEntityUnreifiedRelationship between ModelNamedIndividuals
    */
  type ModelInstanceRelationshipAssertion <: ModelInstanceAssertion

  /**
    * A ModelNamedIndividual identifies an instance of a ModelEntityConcept, ModelEntityReifiedRelationship,
    * ModelStructuredDataType, ModelInstanceDataRelationshipFromEntityToStructure,
    * ModelInstanceDataRelationshipFromStructureToStructure
    */
  type ModelNamedIndividual <: ModelInstanceAssertion

  /**
    * A ModelEntityInstance identifies an instance of a ModelEntityConcept or ModelEntityReifiedRelationship
    */
  type ModelEntityInstance <: ModelNamedIndividual

  /**
    * A ModelInstanceObject identifies an isntance of a ModelEntityConcept
    */
  type ModelInstanceObject <: ModelEntityInstance

  /**
    * A ModelInstanceRelation identifies an instance of a ModelEntityReifiedRelationship
    */
  type ModelInstanceRelation <: ModelEntityInstance

  /**
    * A ModelDataInstance is a value of a ModelDataTypeDefinition
    */
  type ModelDataInstance

  /**
    * A ModelInstanceDataLiteral is a value of a ModelScalarDataType
    */
  type ModelInstanceDataLiteral <: ModelInstanceAssertion with ModelDataInstance

  /**
    * A ModelInstanceDataStructure is a value of a ModelStructuredDataType
    */
  type ModelInstanceDataStructure <: ModelNamedIndividual with ModelDataInstance

  /**
    * A ModelInstanceDataRelationshipFromEntityToScalar asserts
    * the scalar value of a ModelDataRelationshipFromEntityToScalar property relationship
    * for a ModelEntityInstance
    */
  type ModelInstanceDataRelationshipFromEntityToScalar <: ModelInstanceAssertion

  /**
    * A ModelInstanceDataRelationshipFromEntityToStructure asserts
    * the structured value of a ModelDataRelationshipFromEntityToStructure property relationship
    * for a ModelEntityInstance
    */
  type ModelInstanceDataRelationshipFromEntityToStructure <: ModelNamedIndividual with ModelInstanceAssertion

  /**
    * A ModelInstanceDataRelationshipFromStructureToScalar asserts
    * the scalar value of a ModelDataRelationshipFromStructureToScalar property relationship
    * for a ModelInstanceDataStructure
    */
  type ModelInstanceDataRelationshipFromStructureToScalar <: ModelInstanceAssertion

  /**
    * A ModelInstanceDataRelationshipFromStructureToStructure asserts
    * the structured value of a ModelDataRelationshipFromStructureToStructure property relationship
    * for a ModelInstanceDataStructure
    */
  type ModelInstanceDataRelationshipFromStructureToStructure <: ModelNamedIndividual with ModelInstanceAssertion

}


/**
  * A Terminology graph signature is a tuple.
  *
  * @tparam omf OMF Adaptation/Binding.
  */
trait TerminologyGraphSignature[omf <: OMF] {
  val uuid: UUID
  val name: LocalName
  /**
    * the identity of the terminology graph as a container for several descriptions and as the context
    * for extending other terminology graphs
    */
  val iri: omf#IRI
  /**
    * the semantic commitment of this terminology graph (open-world definitions vs. closed-world designations)
    */
  val kind: TerminologyKind
  /**
    * this terminology graph can use or specialize vocabulary terms
    * defined in the transitive closure of imported terminology graphs
    */
  val imports: Iterable[omf#ModelTerminologyGraph]
  /**
    * this terminology graph can be the nested graph for a parent model term concept
    */
  val nesting: Option[omf#ModelEntityConcept]
  /**
    * the aspects described in this terminology graph
    */
  val aspects: Iterable[omf#ModelEntityAspect]
  /**
    * the concepts described in this terminology graph
    */
  val concepts: Iterable[omf#ModelEntityConcept]
  /**
    * the reified relationships described in this terminology graph
    */
  val reifiedRelationships: Iterable[omf#ModelEntityReifiedRelationship]
  /**
    * the unreified relationships described in scope of this terminology graph
    */
  val unreifiedRelationships: Iterable[omf#ModelEntityUnreifiedRelationship]
  /**
    * the scalar datatypes described in this terminology graph
    */
  val scalarDataTypes: Iterable[omf#ModelScalarDataType]
  /**
    * the structured datatypes described in this terminology graph
    */
  val structuredDataTypes: Iterable[omf#ModelStructuredDataType]
  /**
    * the entity to scalar data relationships described in this terminology graph
    */
  val entity2scalarDataRelationships: Iterable[omf#ModelDataRelationshipFromEntityToScalar]
  /**
    * the entity to structured data relationships described in this terminology graph
    */
  val entity2structureDataRelationships: Iterable[omf#ModelDataRelationshipFromEntityToStructure]
  /**
    * the entity to scalar data  relationships described in this terminology graph
    */
  val structure2scalarDataRelationships: Iterable[omf#ModelDataRelationshipFromStructureToScalar]
  /**
    * the entity to scalar data  relationships described in this terminology graph
    */
  val structure2structureDataRelationships: Iterable[omf#ModelDataRelationshipFromStructureToStructure]
  /**
    * the model term axioms asserted in this terminology graph
    */
  val axioms: Iterable[omf#ModelTermAxiom]
  /**
    * The terminology graph axioms asserted in this terminology graph
    */
  val gaxioms: Iterable[omf#TerminologyGraphAxiom]
}

trait EntityConceptSignature[omf <: OMF] {
  val uuid: UUID
  val name: LocalName
  val iri: omf#IRI
  val isAbstract: Boolean
}

trait EntityReifiedRelationshipSignature[omf <: OMF] {
  val uuid: UUID
  val name: LocalName
  val iri: omf#IRI
  val source: omf#ModelEntityDefinition
  val target: omf#ModelEntityDefinition
  val characteristics: Iterable[RelationshipCharacteristics]
  val isAbstract: Boolean
}

trait EntityUnreifiedRelationshipSignature[omf <: OMF] {
  val uuid: UUID
  val name: LocalName
  val iri: omf#IRI
  val source: omf#ModelEntityDefinition
  val target: omf#ModelEntityDefinition
  val characteristics: Iterable[RelationshipCharacteristics]
}