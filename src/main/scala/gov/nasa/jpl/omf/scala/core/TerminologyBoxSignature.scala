package gov.nasa.jpl.omf.scala.core

import java.util.UUID

import gov.nasa.jpl.imce.oml.tables.{AnnotationEntry, AnnotationProperty, LocalName}

import scala.collection.immutable.Set
import scala.Boolean

/**
  * A Terminology signature is a tuple.
  *
  * @tparam omf OMF Adaptation/Binding.
  */
case class TerminologyBoxSignature[omf <: OMF, +S[A] <: scala.collection.Iterable[A], I[A] <: scala.collection.Iterable[A]]
( isBundle: Boolean,
  uuid: UUID,
  name: LocalName,
  /**
    * the identity of the terminology as a container for several descriptions and as the context
    * for extending other terminologies
    */
  iri: omf#IRI,
  /**
    * the semantic commitment of this terminology (open-world definitions vs. closed-world designations)
    */
  kind: TerminologyKind,

  extensions: S[omf#TerminologyExtensionAxiom],
  nesting: S[omf#TerminologyNestingAxiom],
  conceptDesignation: S[omf#ConceptDesignationTerminologyAxiom],
  bundledTerminologies: S[omf#BundledTerminologyAxiom],

  /**
    * the aspects described in this terminology
    */
  aspects: S[omf#Aspect],
  /**
    * the concepts described in this terminology
    */
  concepts: S[omf#Concept],
  /**
    * the reified relationships described in this terminology
    */
  reifiedRelationships: S[omf#ReifiedRelationship],
  /**
    * the unreified relationships described in scope of this terminology
    */
  unreifiedRelationships: S[omf#UnreifiedRelationship],
  /**
    * the scalar datatypes described in this terminology
    */
  scalarDataTypes: S[omf#Scalar],
  /**
    * the structured datatypes described in this terminology
    */
  structuredDataTypes: S[omf#Structure],

  scalarOneOfRestrictions: S[omf#ScalarOneOfRestriction],
  scalarOneOfLiterals: S[omf#ScalarOneOfLiteralAxiom],

  binaryScalarRestrictions: S[omf#BinaryScalarRestriction],
  iriScalarRestrictions: S[omf#IRIScalarRestriction],
  numericScalarRestrictions: S[omf#NumericScalarRestriction],
  plainLiteralScalarRestrictions: S[omf#PlainLiteralScalarRestriction],
  stringScalarRestrictions: S[omf#StringScalarRestriction],
  synonymScalarRestrictions: S[omf#SynonymScalarRestriction],
  timeScalarRestrictions: S[omf#TimeScalarRestriction],

  /**
    * the entity to scalar data relationships described in this terminology
    */
  entityScalarDataProperties: S[omf#EntityScalarDataProperty],
  /**
    * the entity to structured data relationships described in this terminology
    */
  entityStructuredDataProperties: S[omf#EntityStructuredDataProperty],
  /**
    * the entity to scalar data  relationships described in this terminology
    */
  scalarDataProperties: S[omf#ScalarDataProperty],
  /**
    * the entity to scalar data  relationships described in this terminology
    */
  structuredDataProperties: S[omf#StructuredDataProperty],

  /**
    * the model term axioms asserted in this terminology
    */
  axioms: S[omf#Axiom],

  rTAxioms: S[omf#RootConceptTaxonomyAxiom],
  aTAxioms: S[omf#AnonymousConceptTaxonomyAxiom],
  sTAxioms: S[omf#SpecificDisjointConceptAxiom],

  annotationProperties: S[AnnotationProperty],

  annotations: S[(AnnotationProperty, I[AnnotationEntry])]) {

  def importedModules
  (implicit ops: OMFOps[omf])
  : Set[omf#Module]
  = Set.empty[omf#Module] ++
    extensions.map(ops.fromTerminologyExtensionAxiom(_).importedModule) ++
    conceptDesignation.map(ops.fromConceptDesignationTerminologyAxiom(_).importedModule) ++
    bundledTerminologies.map(ops.fromBundledTerminologyAxiom(_).importedModule)

  def terms
  : Set[omf#Term]
  = Set.empty[omf#Term] ++
      aspects ++
      concepts ++
      reifiedRelationships ++
      scalarDataTypes ++
      structuredDataTypes ++
      scalarOneOfRestrictions ++
      binaryScalarRestrictions ++
      plainLiteralScalarRestrictions ++
      stringScalarRestrictions ++
      synonymScalarRestrictions ++
      timeScalarRestrictions ++
      entityScalarDataProperties ++
      entityStructuredDataProperties ++
      scalarDataProperties ++
      structuredDataProperties

}
