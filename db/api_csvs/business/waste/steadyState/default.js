standardDensity=0.72; // kg per metre^3
GWP=21; // IPCC Second Assesment
euler=2.71828; // Euler's number
decayProportion=Math.pow(euler,(-decayRate*timeSinceClosed))-Math.pow(euler,(-decayRate*timeSinceOpened))
gasProduced=wasteDepositionRate*ultimateMethanePotential*decayProportion
gasProduced*standardDensity*GWP*((1-collectionEfficiency)*(1-fractionUncollectedOxidising)+collectionEfficiency*(1-fractionCollectedBurned))