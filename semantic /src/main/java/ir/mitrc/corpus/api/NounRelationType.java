package ir.mitrc.corpus.api;

public enum NounRelationType implements IRelationType {
	Hypernym,
	indirectHypernym,
	Hyponym,
	indirectHyponym,
	MemberOf,
	HasMember,
	PartOf,
	HasPart,
	PorionOf,
	HasPortion,
	instanceOf,
	HasInstance,
	indirectMeronymMemberOf,
	indirectMeronymPartOf,
	indirectMeronymPortionOf,
	indirectHolonymMemberOf,
	indirectHolonymPartOf,
	indirectHolonymPortionOf,
	HypernymOrInstanceOf,
	RelatedVerb;
	
}
