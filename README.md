# BIG CLIMATE DATA ANALYTICS: EFFECTIVE KNOWLEDGE-DISCOVERY FROM COLOMBIA’S WEATHER DATA
This is the code and documention repository for Roland Hudson's dissertation project for a Master of Science in Software Engineering at the University of Liverpool.

The code defines a distributed, machine-learning application that classifies Colombian climate data and provides decision support to environmental designers seeking to understand the spatial and temporal use of low-energy design strategies.

A summary presentation of the project can be seen [here](https://docs.google.com/presentation/d/1QAY6ZUAJ5zlwWwdgNoAhxZlYenV0TnsVtp8n6fcSLdM/edit?usp=sharing)

Results from the evaluation of the project that also demonstrate the online dashboard can be see [here](http://lacunae.io/)

For more detail read the abstract below, for even more detail review the dissertation word document and the astah diagrams in the [documentation folder](../blob/master/documentation).

# Abstract
The aim of this dissertation is to develop a distributed, machine-learning application that classifies Colombian climate data and provides decision support to environmental designers seeking to understand the spatial and temporal use of low-energy design strategies.  These strategies can help provide more comfortable living and working conditions for people using the buildings and reduce the need for heating and cooling, lowering emissions and energy consumption. Implementing these strategies requires understanding the local and regional climate conditions over different periods. In Colombia, a lack of seasons, extreme topographical vari-ations and subtle tropical patterns make identifying localized, low-energy construction strategies complex.

The dissertation provides an overview of literature covering previous relevant research publications and theory, this includes examination of low-energy design strategies and spatiotemporal nature of climate data. The literature review summarises big data tools and systems, relevant to climate data and identifies recent applications of machine learning to classify climates. The analysis and design chapter describes use cases, identifies system requirements and proposes a general system design. The implementation chapter documents how Agile Model Driven Development is used to model, implement and document a software artefact, which integrates a local application with analysis and visualisation in a distributed, cloud-based environment.

The completed system allows users to explore a climatic dataset with the aim of finding spatiotemporal patterns and linking these to low-energy design techniques. A graphical user interface provides tools to configure analytic jobs, create and edit design strategies on a psychrometric chart and monitor the status of cloud resources. Apache Spark provides a distributed framework for processing the data using hierarchical and non-hierarchical clustering techniques.  The system links clusters to design strategies and calculates domain and data-centric indices that indicate clustering performance. 

The evaluation chapter describes the system appraisal process involving a series of analytic experiments that capture clustering specific and domain centric performance metrics.  Both metrics indicate that the artefact can classify the Colombian climate into distant classes that represent climate patterns. Visualisation of the results of the evaluation support this finding by graphically representing known patterns in space and time and linking these with appropriate design strategies. Finally, software walkthroughs with domain experts suggest that with modification of certain controls and re-evaluation of the primary users the application is useful in industry.
