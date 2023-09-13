package dev.dunor.app.arXiv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

class ArxivRepository {

  private val service =
    Retrofit.Builder().baseUrl(ARXIV_BASE_URL).build().create(ArxivService::class.java)

  private fun buildQuery(
    query: SearchQuery
  ): String =
    when (query) {
      is SearchQuery.PARAM -> "${query.key.code}:${query.value}"
      is SearchQuery.AND -> "${buildQuery(query.l)} AND ${buildQuery(query.r)}"
      is SearchQuery.OR -> "${buildQuery(query.l)} OR ${buildQuery(query.r)}"
      is SearchQuery.ANDNOT -> "${buildQuery(query.l)} ANDNOT ${buildQuery(query.r)}"
      is SearchQuery.PRIORITY -> "(${buildQuery(query.query)})"
    }

  // please refer https://info.arxiv.org/help/api/user-manual.html#51-details-of-query-construction for
  // info about how to construct search_query
  enum class SearchQueryKey(val code: String) {
    Title("ti"),
    Author("au"),
    Abstract("abs"),
    Comment("co"),
    JournalReference("jr"),
    SubjectCategory("cat"), // category has a set of ids which can be find in companion object ArxivCategoryData
    ReportNumber("rn"),
    All("all") // all of above
  }

  sealed class SearchQuery {
    data class AND(val l: SearchQuery, val r: SearchQuery): SearchQuery()
    data class OR(val l: SearchQuery, val r: SearchQuery): SearchQuery()
    data class ANDNOT(val l: SearchQuery, val r: SearchQuery): SearchQuery()
    data class PRIORITY(val query: SearchQuery): SearchQuery()
    data class PARAM(val key: SearchQueryKey, val value: String): SearchQuery()
  }

  // build subject category from category data
  fun paramFrom(category: ArxivCategoryData): SearchQuery =
    SearchQuery.PARAM(SearchQueryKey.SubjectCategory, category.code)
  fun paramFrom(category: ArxivCategory, subCategory: String) =
    SearchQuery.PARAM(SearchQueryKey.SubjectCategory, getSubCategory(category, subCategory)!!.code)

  suspend fun query(start: Int, maxResults: Int, query: SearchQuery): ArxivAtomSearchResult {
    return coroutineScope {
      runBlocking(Dispatchers.IO) {
        try {
          val response = service.atomQuery(start, maxResults, buildQuery(query)).execute()
          println("service emit query")
          val parser = ArxivAtomSearchParser(response.body()!!.byteStream())
          println("parser done parsing")
          println("${parser.query} ==> ${parser.updatedTime}")
          return@runBlocking ArxivAtomSearchResult(
            parser.queryId,
            parser.queryLink.url,
            parser.startIndex,
            parser.itemsPerPage,
            parser.totalResults,
            parser.entries.toList()
          )
        } catch (e: Exception) {
          throw ArxivRequestException("Something wrong when do a querying", e)
        }
      }
    }
  }

  class ArxivRequestException(reason: String, e: Exception): Exception(reason, e)

  // get all top level categories
  val mainCategories: List<ArxivCategory>
    get() = ArxivCategory.values().toList()

  // get all sub categories of main category
  fun getSubCategories(mainCategory: ArxivCategory): List<ArxivCategoryData> = when (mainCategory) {
    ArxivCategory.QuantitativeFinance -> categoryQuantitativeFinance
    ArxivCategory.ComputerScience -> categoryComputerScience
    ArxivCategory.Economics -> categoryEconomics
    ArxivCategory.ElectricalEngineeringAndSystemService -> categoryElectricalEngineeringAndSystemService
    ArxivCategory.Mathematics -> categoryMathematics
    ArxivCategory.Statistics -> categoryStatistics
    ArxivCategory.QuantitativeBiology -> categoryQuantitativeBiology
    ArxivCategory.Physics -> categoryPhysics
    ArxivCategory.AstroPhysics -> categoryAstroPhysics
    ArxivCategory.CondensedMatter -> categoryCondensedMatter
    ArxivCategory.PhysicsOther -> categoryPhysicsOther
    ArxivCategory.NonlinearSciences -> categoryNonlinearSciences
  }

  // get a sub category
  fun getSubCategory(mainCategory: ArxivCategory, subCategory: String): ArxivCategoryData? =
    getSubCategories(mainCategory).find { it.category == subCategory }

  interface ArxivService {
    // start is 0 indexed
    @GET("query?sortBy=lastUpdatedDate&sortOrder=descending")
    fun atomQuery(@Query("start") start: Int, @Query("max_results") maxResults: Int, @Query("search_query") query: String): Call<ResponseBody>

  }

  data class ArxivAtomSearchResult(
    val id: String, // unique search id
    val query: String,  // a url which can be used to request same result, as long as data has not been updated
    val startIndex: Int,
    val itemsPerPage: Int,
    val totalResults: Int,
    val results: List<ArxivAtomEntry>
  )

  // this is the place holding all category records of arxiv
  companion object {
    const val ARXIV_BASE_URL = "http://export.arxiv.org/api/"

    private val categoryQuantitativeFinance = listOf(
      ArxivCategoryData("Computational Finance", "q-fin.CP", "Computational methods, including Monte Carlo, PDE, lattice and other numerical methods with applications to financial modeling"),
      ArxivCategoryData("Economics", "q-fin.EC", "q-fin.EC is an alias for econ.GN. Economics, including micro and macro economics, international economics, theory of the firm, labor economics, and other economic topics outside finance"),
      ArxivCategoryData("General Finance", "q-fin.GN", "Development of general quantitative methodologies with applications in finance"),
      ArxivCategoryData("Mathematical Finance", "q-fin.MF", "Mathematical and analytical methods of finance, including stochastic, probabilistic and functional analysis, algebraic, geometric and other methods"),
      ArxivCategoryData("Portfolio Management", "q-fin.PM", "Security selection and optimization, capital allocation, investment strategies and performance measurement"),
      ArxivCategoryData("Pricing of Securities", "q-fin.PR", "Valuation and hedging of financial securities, their derivatives, and structured products"),
      ArxivCategoryData("Risk Management", "q-fin.RM", "Measurement and management of financial risks in trading, banking, insurance, corporate and other applications"),
      ArxivCategoryData("Statistical Finance", "q-fin.ST", "Statistical, econometric and econophysics analyses with applications to financial markets and economic data"),
      ArxivCategoryData("Trading and Market Microstructure", "q-fin.TR", "Market microstructure, liquidity, exchange and auction design, automated trading, agent-based modeling and market-making")
    )

    private val categoryEconomics = listOf(
      ArxivCategoryData("Econometrics", "econ.EM", "Econometric Theory, Micro-Econometrics, Macro-Econometrics, Empirical Content of Economic Relations discovered via New Methods, Methodological Aspects of the Application of Statistical Inference to Economic Data."),
      ArxivCategoryData("General Economics", "econ.GN", "General methodological, applied, and empirical contributions to economics."),
      ArxivCategoryData("Theoretical Economics", "econ.TH", "Includes theoretical contributions to Contract Theory, Decision Theory, Game Theory, General Equilibrium, Growth, Learning and Evolution, Macroeconomics, Market and Mechanism Design, and Social Choice.")
    )

    private val categoryComputerScience = listOf(
      ArxivCategoryData("Artificial Intelligence", "cs.AI", "Covers all areas of AI except Vision, Robotics, Machine Learning, Multiagent Systems, and Computation and Language (Natural Language Processing), which have separate subject areas. In particular, includes Expert Systems, Theorem Proving (although this may overlap with Logic in Computer Science), Knowledge Representation, Planning, and Uncertainty in AI. Roughly includes material in ACM Subject Classes I.2.0, I.2.1, I.2.3, I.2.4, I.2.8, and I.2.11."),
      ArxivCategoryData("Hardware Architecture", "cs.AR", "Covers systems organization and hardware architecture. Roughly includes material in ACM Subject Classes C.0, C.1, and C.5."),
      ArxivCategoryData("Computational Complexity", "cs.CC", "Covers models of computation, complexity classes, structural complexity, complexity tradeoffs, upper and lower bounds. Roughly includes material in ACM Subject Classes F.1 (computation by abstract devices), F.2.3 (tradeoffs among complexity measures), and F.4.3 (formal languages), although some material in formal languages may be more appropriate for Logic in Computer Science. Some material in F.2.1 and F.2.2, may also be appropriate here, but is more likely to have Data Structures and Algorithms as the primary subject area."),
      ArxivCategoryData("Computational Engineering, Finance, and Science", "cs.CE", "Covers applications of computer science to the mathematical modeling of complex systems in the fields of science, engineering, and finance. Papers here are interdisciplinary and applications-oriented, focusing on techniques and tools that enable challenging computational simulations to be performed, for which the use of supercomputers or distributed computing platforms is often required. Includes material in ACM Subject Classes J.2, J.3, and J.4 (economics)."),
      ArxivCategoryData("Computational Geometry", "cs.CG", "Roughly includes material in ACM Subject Classes I.3.5 and F.2.2."),
      ArxivCategoryData("Computation and Language", "cs.CL", "Covers natural language processing. Roughly includes material in ACM Subject Class I.2.7. Note that work on artificial languages (programming languages, logics, formal systems) that does not explicitly address natural-language issues broadly construed (natural-language processing, computational linguistics, speech, text retrieval, etc.) is not appropriate for this area."),
      ArxivCategoryData("Cryptography and Security", "cs.CR", "Covers all areas of cryptography and security including authentication, public key cryptosytems, proof-carrying code, etc. Roughly includes material in ACM Subject Classes D.4.6 and E.3."),
      ArxivCategoryData("Computer Vision and Pattern Recognition", "cs.CV", "Covers image processing, computer vision, pattern recognition, and scene understanding. Roughly includes material in ACM Subject Classes I.2.10, I.4, and I.5."),
      ArxivCategoryData("Computers and Society", "cs.CY", "Covers impact of computers on society, computer ethics, information technology and public policy, legal aspects of computing, computers and education. Roughly includes material in ACM Subject Classes K.0, K.2, K.3, K.4, K.5, and K.7."),
      ArxivCategoryData("Databases", "cs.DB", "Covers database management, datamining, and data processing. Roughly includes material in ACM Subject Classes E.2, E.5, H.0, H.2, and J.1."),
      ArxivCategoryData("Distributed, Parallel, and Cluster Computing", "cs.DC", "Covers fault-tolerance, distributed algorithms, stabilility, parallel computation, and cluster computing. Roughly includes material in ACM Subject Classes C.1.2, C.1.4, C.2.4, D.1.3, D.4.5, D.4.7, E.1."),
      ArxivCategoryData("Digital Libraries", "cs.DL", "Covers all aspects of the digital library design and document and text creation. Note that there will be some overlap with Information Retrieval (which is a separate subject area). Roughly includes material in ACM Subject Classes H.3.5, H.3.6, H.3.7, I.7."),
      ArxivCategoryData("Discrete Mathematics", "cs.DM", "Covers combinatorics, graph theory, applications of probability. Roughly includes material in ACM Subject Classes G.2 and G.3."),
      ArxivCategoryData("Data Structures and Algorithms", "cs.DS", "Covers data structures and analysis of algorithms. Roughly includes material in ACM Subject Classes E.1, E.2, F.2.1, and F.2.2."),
      ArxivCategoryData("Emerging Technologies", "cs.ET", "Covers approaches to information processing (computing, communication, sensing) and bio-chemical analysis based on alternatives to silicon CMOS-based technologies, such as nanoscale electronic, photonic, spin-based, superconducting, mechanical, bio-chemical and quantum technologies (this list is not exclusive). Topics of interest include (1) building blocks for emerging technologies, their scalability and adoption in larger systems, including integration with traditional technologies, (2) modeling, design and optimization of novel devices and systems, (3) models of computation, algorithm design and programming for emerging technologies."),
      ArxivCategoryData("Formal Languages and Automata Theory", "cs.FL", "Covers automata theory, formal language theory, grammars, and combinatorics on words. This roughly corresponds to ACM Subject Classes F.1.1, and F.4.3. Papers dealing with computational complexity should go to cs.CC; papers dealing with logic should go to cs.LO."),
      ArxivCategoryData("General Literature", "cs.GL", "Covers introductory material, survey material, predictions of future trends, biographies, and miscellaneous computer-science related material. Roughly includes all of ACM Subject Class A, except it does not include conference proceedings (which will be listed in the appropriate subject area)."),
      ArxivCategoryData("Graphics", "cs.GR", "Covers all aspects of computer graphics. Roughly includes material in all of ACM Subject Class I.3, except that I.3.5 is is likely to have Computational Geometry as the primary subject area."),
      ArxivCategoryData("Computer Science and Game Theory", "cs.GT", "Covers all theoretical and applied aspects at the intersection of computer science and game theory, including work in mechanism design, learning in games (which may overlap with Learning), foundations of agent modeling in games (which may overlap with Multiagent systems), coordination, specification and formal methods for non-cooperative computational environments. The area also deals with applications of game theory to areas such as electronic commerce."),
      ArxivCategoryData("Human-Computer Interaction", "cs.HC", "Covers human factors, user interfaces, and collaborative computing. Roughly includes material in ACM Subject Classes H.1.2 and all of H.5, except for H.5.1, which is more likely to have Multimedia as the primary subject area."),
      ArxivCategoryData("Information Retrieval", "cs.IR", "Covers indexing, dictionaries, retrieval, content and analysis. Roughly includes material in ACM Subject Classes H.3.0, H.3.1, H.3.2, H.3.3, and H.3.4."),
      ArxivCategoryData("Information Theory", "cs.IT", "Covers theoretical and experimental aspects of information theory and coding. Includes material in ACM Subject Class E.4 and intersects with H.1.1."),
      ArxivCategoryData("Machine Learning", "cs.LG", "Papers on all aspects of machine learning research (supervised, unsupervised, reinforcement learning, bandit problems, and so on) including also robustness, explanation, fairness, and methodology. cs.LG is also an appropriate primary category for applications of machine learning methods."),
      ArxivCategoryData("Logic in Computer Science", "cs.LO", "Covers all aspects of logic in computer science, including finite model theory, logics of programs, modal logic, and program verification. Programming language semantics should have Programming Languages as the primary subject area. Roughly includes material in ACM Subject Classes D.2.4, F.3.1, F.4.0, F.4.1, and F.4.2; some material in F.4.3 (formal languages) may also be appropriate here, although Computational Complexity is typically the more appropriate subject area."),
      ArxivCategoryData("Multiagent Systems", "cs.MA", "Covers multiagent systems, distributed artificial intelligence, intelligent agents, coordinated interactions. and practical applications. Roughly covers ACM Subject Class I.2.11."),
      ArxivCategoryData("Multimedia", "cs.MM", "Roughly includes material in ACM Subject Class H.5.1."),
      ArxivCategoryData("Mathematical Software", "cs.MS", "Roughly includes material in ACM Subject Class G.4."),
      ArxivCategoryData("Numerical Analysis", "cs.NA", "cs.NA is an alias for math.NA. Roughly includes material in ACM Subject Class G.1."),
      ArxivCategoryData("Neural and Evolutionary Computing", "cs.NE", "Covers neural networks, connectionism, genetic algorithms, artificial life, adaptive behavior. Roughly includes some material in ACM Subject Class C.1.3, I.2.6, I.5."),
      ArxivCategoryData("Networking and Internet Architecture", "cs.NI", "Covers all aspects of computer communication networks, including network architecture and design, network protocols, and internetwork standards (like TCP/IP). Also includes topics, such as web caching, that are directly relevant to Internet architecture and performance. Roughly includes all of ACM Subject Class C.2 except C.2.4, which is more likely to have Distributed, Parallel, and Cluster Computing as the primary subject area."),
      ArxivCategoryData("Other Computer Science", "cs.OH", "This is the classification to use for documents that do not fit anywhere else."),
      ArxivCategoryData("Operating Systems", "cs.OS", "Roughly includes material in ACM Subject Classes D.4.1, D.4.2., D.4.3, D.4.4, D.4.5, D.4.7, and D.4.9."),
      ArxivCategoryData("Performance", "cs.PF", "Covers performance measurement and evaluation, queueing, and simulation. Roughly includes material in ACM Subject Classes D.4.8 and K.6.2."),
      ArxivCategoryData("Programming Languages", "cs.PL", "Covers programming language semantics, language features, programming approaches (such as object-oriented programming, functional programming, logic programming). Also includes material on compilers oriented towards programming languages; other material on compilers may be more appropriate in Architecture (AR). Roughly includes material in ACM Subject Classes D.1 and D.3."),
      ArxivCategoryData("Robotics", "cs.RO", "Roughly includes material in ACM Subject Class I.2.9."),
      ArxivCategoryData("Symbolic Computation", "cs.SC", "Roughly includes material in ACM Subject Class I.1."),
      ArxivCategoryData("Sound", "cs.SD", "Covers all aspects of computing with sound, and sound as an information channel. Includes models of sound, analysis and synthesis, audio user interfaces, sonification of data, computer music, and sound signal processing. Includes ACM Subject Class H.5.5, and intersects with H.1.2, H.5.1, H.5.2, I.2.7, I.5.4, I.6.3, J.5, K.4.2."),
      ArxivCategoryData("Software Engineering", "cs.SE", "Covers design tools, software metrics, testing and debugging, programming environments, etc. Roughly includes material in all of ACM Subject Classes D.2, except that D.2.4 (program verification) should probably have Logics in Computer Science as the primary subject area."),
      ArxivCategoryData("Social and Information Networks", "cs.SI", "Covers the design, analysis, and modeling of social and information networks, including their applications for on-line information access, communication, and interaction, and their roles as datasets in the exploration of questions in these and other domains, including connections to the social and biological sciences. Analysis and modeling of such networks includes topics in ACM Subject classes F.2, G.2, G.3, H.2, and I.2; applications in computing include topics in H.3, H.4, and H.5; and applications at the interface of computing and other disciplines include topics in J.1--J.7. Papers on computer communication systems and network protocols (e.g. TCP/IP) are generally a closer fit to the Networking and Internet Architecture (cs.NI) category."),
      ArxivCategoryData("Systems and Control", "cs.SY", "cs.SY is an alias for eess.SY. This section includes theoretical and experimental research covering all facets of automatic control systems. The section is focused on methods of control system analysis and design using tools of modeling, simulation and optimization. Specific areas of research include nonlinear, distributed, adaptive, stochastic and robust control in addition to hybrid and discrete event systems. Application areas include automotive and aerospace control systems, network control, biological systems, multiagent and cooperative control, robotics, reinforcement learning, sensor networks, control of cyber-physical and energy-related systems, and control of computing systems."),
    )

    private val categoryElectricalEngineeringAndSystemService = listOf(
      ArxivCategoryData("Audio and Speech Processing", "eess.AS", "Theory and methods for processing signals representing audio, speech, and language, and their applications. This includes analysis, synthesis, enhancement, transformation, classification and interpretation of such signals as well as the design, development, and evaluation of associated signal processing systems. Machine learning and pattern analysis applied to any of the above areas is also welcome. Specific topics of interest include: auditory modeling and hearing aids; acoustic beamforming and source localization; classification of acoustic scenes; speaker separation; active noise control and echo cancellation; enhancement; de-reverberation; bioacoustics; music signals analysis, synthesis and modification; music information retrieval; audio for multimedia and joint audio-video processing; spoken and written language modeling, segmentation, tagging, parsing, understanding, and translation; text mining; speech production, perception, and psychoacoustics; speech analysis, synthesis, and perceptual modeling and coding; robust speech recognition; speaker recognition and characterization; deep learning, online learning, and graphical models applied to speech, audio, and language signals; and implementation aspects ranging from system architecture to fast algorithms."),
      ArxivCategoryData("Image and Video Processing", "eess.IV", "Theory, algorithms, and architectures for the formation, capture, processing, communication, analysis, and display of images, video, and multidimensional signals in a wide variety of applications. Topics of interest include: mathematical, statistical, and perceptual image and video modeling and representation; linear and nonlinear filtering, de-blurring, enhancement, restoration, and reconstruction from degraded, low-resolution or tomographic data; lossless and lossy compression and coding; segmentation, alignment, and recognition; image rendering, visualization, and printing; computational imaging, including ultrasound, tomographic and magnetic resonance imaging; and image and video analysis, synthesis, storage, search and retrieval."),
      ArxivCategoryData("Signal Processing", "eess.SP", "Theory, algorithms, performance analysis and applications of signal and data analysis, including physical modeling, processing, detection and parameter estimation, learning, mining, retrieval, and information extraction. The term \"signal\" includes speech, audio, sonar, radar, geophysical, physiological, (bio-) medical, image, video, and multimodal natural and man-made signals, including communication signals and data. Topics of interest include: statistical signal processing, spectral estimation and system identification; filter design, adaptive filtering / stochastic learning; (compressive) sampling, sensing, and transform-domain methods including fast algorithms; signal processing for machine learning and machine learning for signal processing applications; in-network and graph signal processing; convex and nonconvex optimization methods for signal processing applications; radar, sonar, and sensor array beamforming and direction finding; communications signal processing; low power, multi-core and system-on-chip signal processing; sensing, communication, analysis and optimization for cyber-physical systems such as power grids and the Internet of Things."),
      ArxivCategoryData("Systems and Control", "eess.SY", "This section includes theoretical and experimental research covering all facets of automatic control systems. The section is focused on methods of control system analysis and design using tools of modeling, simulation and optimization. Specific areas of research include nonlinear, distributed, adaptive, stochastic and robust control in addition to hybrid and discrete event systems. Application areas include automotive and aerospace control systems, network control, biological systems, multiagent and cooperative control, robotics, reinforcement learning, sensor networks, control of cyber-physical and energy-related systems, and control of computing systems.")
    )

    private val categoryMathematics = listOf(
      ArxivCategoryData("Commutative Algebra", "math.AC", "Commutative rings, modules, ideals, homological algebra, computational aspects, invariant theory, connections to algebraic geometry and combinatorics"),
      ArxivCategoryData("Algebraic Geometry", "math.AG", "Algebraic varieties, stacks, sheaves, schemes, moduli spaces, complex geometry, quantum cohomology"),
      ArxivCategoryData("Analysis of PDEs", "math.AP", "Existence and uniqueness, boundary conditions, linear and non-linear operators, stability, soliton theory, integrable PDE's, conservation laws, qualitative dynamics"),
      ArxivCategoryData("Algebraic Topology", "math.AT", "Homotopy theory, homological algebra, algebraic treatments of manifolds"),
      ArxivCategoryData("Classical Analysis and ODEs", "math.CA", "Special functions, orthogonal polynomials, harmonic analysis, ODE's, differential relations, calculus of variations, approximations, expansions, asymptotics"),
      ArxivCategoryData("Combinatorics", "math.CO", "Discrete mathematics, graph theory, enumeration, combinatorial optimization, Ramsey theory, combinatorial game theory"),
      ArxivCategoryData("Category Theory", "math.CT", "Enriched categories, topoi, abelian categories, monoidal categories, homological algebra"),
      ArxivCategoryData("Complex Variables", "math.CV", "Holomorphic functions, automorphic group actions and forms, pseudoconvexity, complex geometry, analytic spaces, analytic sheaves"),
      ArxivCategoryData("Differential Geometry", "math.DG", "Complex, contact, Riemannian, pseudo-Riemannian and Finsler geometry, relativity, gauge theory, global analysis"),
      ArxivCategoryData("Dynamical Systems", "math.DS", "Dynamics of differential equations and flows, mechanics, classical few-body problems, iterations, complex dynamics, delayed differential equations"),
      ArxivCategoryData("Functional Analysis", "math.FA", "Banach spaces, function spaces, real functions, integral transforms, theory of distributions, measure theory"),
      ArxivCategoryData("General Mathematics", "math.GM", "Mathematical material of general interest, topics not covered elsewhere"),
      ArxivCategoryData("General Topology", "math.GN", "Continuum theory, point-set topology, spaces with algebraic structure, foundations, dimension theory, local and global properties"),
      ArxivCategoryData("Group Theory", "math.GR", "Finite groups, topological groups, representation theory, cohomology, classification and structure"),
      ArxivCategoryData("Geometric Topology", "math.GT", "Manifolds, orbifolds, polyhedra, cell complexes, foliations, geometric structures"),
      ArxivCategoryData("History and Overview", "math.HO", "Biographies, philosophy of mathematics, mathematics education, recreational mathematics, communication of mathematics, ethics in mathematics"),
      ArxivCategoryData("Information Theory", "math.IT", "math.IT is an alias for cs.IT. Covers theoretical and experimental aspects of information theory and coding."),
      ArxivCategoryData("K-Theory and Homology", "math.KT", "Algebraic and topological K-theory, relations with topology, commutative algebra, and operator algebras"),
      ArxivCategoryData("Logic", "math.LO", "Logic, set theory, point-set topology, formal mathematics"),
      ArxivCategoryData("Metric Geometry", "math.MG", "Euclidean, hyperbolic, discrete, convex, coarse geometry, comparisons in Riemannian geometry, symmetric spaces"),
      ArxivCategoryData("Mathematical Physics", "math.MP", "math.MP is an alias for math-ph. Articles in this category focus on areas of research that illustrate the application of mathematics to problems in physics, develop mathematical methods for such applications, or provide mathematically rigorous formulations of existing physical theories. Submissions to math-ph should be of interest to both physically oriented mathematicians and mathematically oriented physicists; submissions which are primarily of interest to theoretical physicists or to mathematicians should probably be directed to the respective physics/math categories"),
      ArxivCategoryData("Numerical Analysis", "math.NA", "Numerical algorithms for problems in analysis and algebra, scientific computation"),
      ArxivCategoryData("Number Theory", "math.NT", "Prime numbers, diophantine equations, analytic number theory, algebraic number theory, arithmetic geometry, Galois theory"),
      ArxivCategoryData("Operator Algebras", "math.OA", "Algebras of operators on Hilbert space, C^*-algebras, von Neumann algebras, non-commutative geometry"),
      ArxivCategoryData("Optimization and Control", "math.OC", "Operations research, linear programming, control theory, systems theory, optimal control, game theory"),
      ArxivCategoryData("Probability", "math.PR", "Theory and applications of probability and stochastic processes: e.g. central limit theorems, large deviations, stochastic differential equations, models from statistical mechanics, queuing theory"),
      ArxivCategoryData("Quantum Algebra", "math.QA", "Quantum groups, skein theories, operadic and diagrammatic algebra, quantum field theory"),
      ArxivCategoryData("Rings and Algebras", "math.RA", "Non-commutative rings and algebras, non-associative algebras, universal algebra and lattice theory, linear algebra, semigroups"),
      ArxivCategoryData("Representation Theory", "math.RT", "Linear representations of algebras and groups, Lie theory, associative algebras, multilinear algebra"),
      ArxivCategoryData("Symplectic Geometry", "math.SG", "Hamiltonian systems, symplectic flows, classical integrable systems"),
      ArxivCategoryData("Spectral Theory", "math.SP", "Schrodinger operators, operators on manifolds, general differential operators, numerical studies, integral operators, discrete models, resonances, non-self-adjoint operators, random operators/matrices"),
      ArxivCategoryData("Statistics Theory", "math.ST", "Applied, computational and theoretical statistics: e.g. statistical inference, regression, time series, multivariate analysis, data analysis, Markov chain Monte Carlo, design of experiments, case studies"),
    )

    private val categoryStatistics = listOf(
      ArxivCategoryData("Applications", "stat.AP", "Biology, Education, Epidemiology, Engineering, Environmental Sciences, Medical, Physical Sciences, Quality Control, Social Sciences"),
      ArxivCategoryData("Computation", "stat.CO", "Algorithms, Simulation, Visualization"),
      ArxivCategoryData("Methodology", "stat.ME", "Design, Surveys, Model Selection, Multiple Testing, Multivariate Methods, Signal and Image Processing, Time Series, Smoothing, Spatial Statistics, Survival Analysis, Nonparametric and Semiparametric Methods"),
      ArxivCategoryData("Machine Learning", "stat.ML", "Covers machine learning papers (supervised, unsupervised, semi-supervised learning, graphical models, reinforcement learning, bandits, high dimensional inference, etc.) with a statistical or theoretical grounding"),
      ArxivCategoryData("Other Statistics", "stat.OT", "Work in statistics that does not fit into the other stat classifications"),
      ArxivCategoryData("Statistics Theory", "stat.TH", "stat.TH is an alias for math.ST. Asymptotics, Bayesian Inference, Decision Theory, Estimation, Foundations, Inference, Testing."),
    )

    private val categoryQuantitativeBiology = listOf(
      ArxivCategoryData("Biomolecules", "q-bio.BM", "DNA, RNA, proteins, lipids, etc.; molecular structures and folding kinetics; molecular interactions; single-molecule manipulation."),
      ArxivCategoryData("Cell Behavior", "q-bio.CB", "Cell-cell signaling and interaction; morphogenesis and development; apoptosis; bacterial conjugation; viral-host interaction; immunology"),
      ArxivCategoryData("Genomics", "q-bio.GN", "DNA sequencing and assembly; gene and motif finding; RNA editing and alternative splicing; genomic structure and processes (replication, transcription, methylation, etc); mutational processes."),
      ArxivCategoryData("Molecular Networks", "q-bio.MN", "Gene regulation, signal transduction, proteomics, metabolomics, gene and enzymatic networks"),
      ArxivCategoryData("Neurons and Cognition", "q-bio.NC", "Synapse, cortex, neuronal dynamics, neural network, sensorimotor control, behavior, attention"),
      ArxivCategoryData("Other Quantitative Biology", "q-bio.OT", "Work in quantitative biology that does not fit into the other q-bio classifications"),
      ArxivCategoryData("Populations and Evolution", "q-bio.PE", "Population dynamics, spatio-temporal and epidemiological models, dynamic speciation, co-evolution, biodiversity, foodwebs, aging; molecular evolution and phylogeny; directed evolution; origin of life"),
      ArxivCategoryData("Quantitative Methods", "q-bio.QM", "All experimental, numerical, statistical and mathematical contributions of value to biology"),
      ArxivCategoryData("Subcellular Processes", "q-bio.SC", "Assembly and control of subcellular structures (channels, organelles, cytoskeletons, capsules, etc.); molecular motors, transport, subcellular localization; mitosis and meiosis"),
      ArxivCategoryData("Tissues and Organs", "q-bio.TO", "Blood flow in vessels, biomechanics of bones, electrical waves, endocrine system, tumor growth"),
    )

    private val categoryPhysics = listOf(
      ArxivCategoryData("Accelerator Physics", "physics.acc-ph", "Accelerator theory and simulation. Accelerator technology. Accelerator experiments. Beam Physics. Accelerator design and optimization. Advanced accelerator concepts. Radiation sources including synchrotron light sources and free electron lasers. Applications of accelerators."),
      ArxivCategoryData("Atmospheric and Oceanic Physics", "physics.ao-ph", "Atmospheric and oceanic physics and physical chemistry, biogeophysics, and climate science"),
      ArxivCategoryData("Applied Physics", "physics.app-ph", "Applications of physics to new technology, including electronic devices, optics, photonics, microwaves, spintronics, advanced materials, metamaterials, nanotechnology, and energy sciences."),
      ArxivCategoryData("Atomic and Molecular Clusters", "physics.atm-clus", "Atomic and molecular clusters, nanoparticles: geometric, electronic, optical, chemical, magnetic properties, shell structure, phase transitions, optical spectroscopy, mass spectrometry, photoelectron spectroscopy, ionization potential, electron affinity, interaction with intense light pulses, electron diffraction, light scattering, ab initio calculations, DFT theory, fragmentation, Coulomb explosion, hydrodynamic expansion."),
      ArxivCategoryData("Atomic Physics", "physics.atom-ph", "Atomic and molecular structure, spectra, collisions, and data. Atoms and molecules in external fields. Molecular dynamics and coherent and optical control. Cold atoms and molecules. Cold collisions. Optical lattices."),
      ArxivCategoryData("Biological Physics", "physics.bio-ph", "Molecular biophysics, cellular biophysics, neurological biophysics, membrane biophysics, single-molecule biophysics, ecological biophysics, quantum phenomena in biological systems (quantum biophysics), theoretical biophysics, molecular dynamics/modeling and simulation, game theory, biomechanics, bioinformatics, microorganisms, virology, evolution, biophysical methods."),
      ArxivCategoryData("Chemical Physics", "physics.chem-ph", "Experimental, computational, and theoretical physics of atoms, molecules, and clusters - Classical and quantum description of states, processes, and dynamics; spectroscopy, electronic structure, conformations, reactions, interactions, and phases. Chemical thermodynamics. Disperse systems. High pressure chemistry. Solid state chemistry. Surface and interface chemistry."),
      ArxivCategoryData("Classical Physics", "physics.class-ph", "Newtonian and relativistic dynamics; many particle systems; planetary motions; chaos in classical dynamics. Maxwell's equations and dynamics of charged systems and electromagnetic forces in materials. Vibrating systems such as membranes and cantilevers; optomechanics. Classical waves, including acoustics and elasticity; physics of music and musical instruments. Classical thermodynamics and heat flow problems."),
      ArxivCategoryData("Computational Physics", "physics.comp-ph", "All aspects of computational science applied to physics."),
      ArxivCategoryData("Data Analysis, Statistics and Probability", "physics.data-an", "Methods, software and hardware for physics data analysis: data processing and storage; measurement methodology; statistical and mathematical aspects such as parametrization and uncertainties."),
      ArxivCategoryData("Physics Education", "physics.ed-ph", "Report of results of a research study, laboratory experience, assessment or classroom practice that represents a way to improve teaching and learning in physics. Also, report on misconceptions of students, textbook errors, and other similar information relative to promoting physics understanding."),
      ArxivCategoryData("Fluid Dynamics", "physics.flu-dyn", "Turbulence, instabilities, incompressible/compressible flows, reacting flows. Aero/hydrodynamics, fluid-structure interactions, acoustics. Biological fluid dynamics, micro/nanofluidics, interfacial phenomena. Complex fluids, suspensions and granular flows, porous media flows. Geophysical flows, thermoconvective and stratified flows. Mathematical and computational methods for fluid dynamics, fluid flow models, experimental techniques."),
      ArxivCategoryData("General Physics", "physics.gen-ph", "Description coming soon"),
      ArxivCategoryData("Geophysics", "physics.geo-ph", "Atmospheric physics. Biogeosciences. Computational geophysics. Geographic location. Geoinformatics. Geophysical techniques. Hydrospheric geophysics. Magnetospheric physics. Mathematical geophysics. Planetology. Solar system. Solid earth geophysics. Space plasma physics. Mineral physics. High pressure physics."),
      ArxivCategoryData("History and Philosophy of Physics", "physics.hist-ph", "History and philosophy of all branches of physics, astrophysics, and cosmology, including appreciations of physicists."),
      ArxivCategoryData("Instrumentation and Detectors", "physics.ins-det", "Instrumentation and Detectors for research in natural science, including optical, molecular, atomic, nuclear and particle physics instrumentation and the associated electronics, services, infrastructure and control equipment."),
      ArxivCategoryData("Medical Physics", "physics.med-ph", "Radiation therapy. Radiation dosimetry. Biomedical imaging modelling. Reconstruction, processing, and analysis. Biomedical system modelling and analysis. Health physics. New imaging or therapy modalities."),
      ArxivCategoryData("Optics", "physics.optics", "Adaptive optics. Astronomical optics. Atmospheric optics. Biomedical optics. Cardinal points. Collimation. Doppler effect. Fiber optics. Fourier optics. Geometrical optics (Gradient index optics. Holography. Infrared optics. Integrated optics. Laser applications. Laser optical systems. Lasers. Light amplification. Light diffraction. Luminescence. Microoptics. Nano optics. Ocean optics. Optical computing. Optical devices. Optical imaging. Optical materials. Optical metrology. Optical microscopy. Optical properties. Optical signal processing. Optical testing techniques. Optical wave propagation. Paraxial optics. Photoabsorption. Photoexcitations. Physical optics. Physiological optics. Quantum optics. Segmented optics. Spectra. Statistical optics. Surface optics. Ultrafast optics. Wave optics. X-ray optics."),
      ArxivCategoryData("Plasma Physics", "physics.plasm-ph", "Fundamental plasma physics. Magnetically Confined Plasmas (includes magnetic fusion energy research). High Energy Density Plasmas (inertial confinement plasmas, laser-plasma interactions). Ionospheric, Heliophysical, and Astrophysical plasmas (includes sun and solar system plasmas). Lasers, Accelerators, and Radiation Generation. Low temperature plasmas and plasma applications (include dusty plasmas, semiconductor etching, plasma-based nanotechnology, medical applications). Plasma Diagnostics, Engineering and Enabling Technologies (includes fusion reactor design, heating systems, diagnostics, experimental techniques)"),
      ArxivCategoryData("Popular Physics", "physics.pop-ph", "Description coming soon"),
      ArxivCategoryData("Physics and Society", "physics.soc-ph", "Structure, dynamics and collective behavior of societies and groups (human or otherwise). Quantitative analysis of social networks and other complex networks. Physics and engineering of infrastructure and systems of broad societal impact (e.g., energy grids, transportation networks)."),
      ArxivCategoryData("Space Physics", "physics.space-ph", "Space plasma physics. Heliophysics. Space weather. Planetary magnetospheres, ionospheres and magnetotail. Auroras. Interplanetary space. Cosmic rays. Synchrotron radiation. Radio astronomy."),
    )

    private val categoryAstroPhysics = listOf(
      ArxivCategoryData("Cosmology and Nongalactic Astrophysics", "astro-ph.CO", "Phenomenology of early universe, cosmic microwave background, cosmological parameters, primordial element abundances, extragalactic distance scale, large-scale structure of the universe. Groups, superclusters, voids, intergalactic medium. Particle astrophysics: dark energy, dark matter, baryogenesis, leptogenesis, inflationary models, reheating, monopoles, WIMPs, cosmic strings, primordial black holes, cosmological gravitational radiation"),
      ArxivCategoryData("Earth and Planetary Astrophysics", "astro-ph.EP", "Interplanetary medium, planetary physics, planetary astrobiology, extrasolar planets, comets, asteroids, meteorites. Structure and formation of the solar system"),
      ArxivCategoryData("Astrophysics of Galaxies", "astro-ph.GA", "Phenomena pertaining to galaxies or the Milky Way. Star clusters, HII regions and planetary nebulae, the interstellar medium, atomic and molecular clouds, dust. Stellar populations. Galactic structure, formation, dynamics. Galactic nuclei, bulges, disks, halo. Active Galactic Nuclei, supermassive black holes, quasars. Gravitational lens systems. The Milky Way and its contents"),
      ArxivCategoryData("High Energy Astrophysical Phenomena", "astro-ph.HE", "Cosmic ray production, acceleration, propagation, detection. Gamma ray astronomy and bursts, X-rays, charged particles, supernovae and other explosive phenomena, stellar remnants and accretion systems, jets, microquasars, neutron stars, pulsars, black holes"),
      ArxivCategoryData("Instrumentation and Methods for Astrophysics", "astro-ph.IM", "Detector and telescope design, experiment proposals. Laboratory Astrophysics. Methods for data analysis, statistical methods. Software, database design"),
      ArxivCategoryData("Solar and Stellar Astrophysics", "astro-ph.SR", "White dwarfs, brown dwarfs, cataclysmic variables. Star formation and protostellar systems, stellar astrobiology, binary and multiple systems of stars, stellar evolution and structure, coronas. Central stars of planetary nebulae. Helioseismology, solar neutrinos, production and detection of gravitational radiation from stellar systems"),
    )

    private val categoryCondensedMatter = listOf(
      ArxivCategoryData("Disordered Systems and Neural Networks", "cond-mat.dis-nn", "Glasses and spin glasses; properties of random, aperiodic and quasiperiodic systems; transport in disordered media; localization; phenomena mediated by defects and disorder; neural networks"),
      ArxivCategoryData("Mesoscale and Nanoscale Physics", "cond-mat.mes-hall", "Semiconducting nanostructures: quantum dots, wires, and wells. Single electronics, spintronics, 2d electron gases, quantum Hall effect, nanotubes, graphene, plasmonic nanostructures"),
      ArxivCategoryData("Materials Science", "cond-mat.mtrl-sci", "Techniques, synthesis, characterization, structure. Structural phase transitions, mechanical properties, phonons. Defects, adsorbates, interfaces"),
      ArxivCategoryData("Other Condensed Matter", "cond-mat.other", "Work in condensed matter that does not fit into the other cond-mat classifications"),
      ArxivCategoryData("Quantum Gases", "cond-mat.quant-gas", "Ultracold atomic and molecular gases, Bose-Einstein condensation, Feshbach resonances, spinor condensates, optical lattices, quantum simulation with cold atoms and molecules, macroscopic interference phenomena"),
      ArxivCategoryData("Soft Condensed Matter", "cond-mat.soft", "Membranes, polymers, liquid crystals, glasses, colloids, granular matter"),
      ArxivCategoryData("Statistical Mechanics", "cond-mat.stat-mech", "Phase transitions, thermodynamics, field theory, non-equilibrium phenomena, renormalization group and scaling, integrable models, turbulence"),
      ArxivCategoryData("Strongly Correlated Electrons", "cond-mat.str-el", "Quantum magnetism, non-Fermi liquids, spin liquids, quantum criticality, charge density waves, metal-insulator transitions"),
      ArxivCategoryData("Superconductivity", "cond-mat.supr-con", "Superconductivity: theory, models, experiment. Superflow in helium"),
    )

    private val categoryPhysicsOther = listOf(
      ArxivCategoryData("General Relativity and Quantum Cosmology", "gr-qc", "General Relativity and Quantum Cosmology Areas of gravitational physics, including experiments and observations related to the detection and interpretation of gravitational waves, experimental tests of gravitational theories, computational general relativity, relativistic astrophysics, solutions to Einstein's equations and their properties, alternative theories of gravity, classical and quantum cosmology, and quantum gravity."),
      ArxivCategoryData("High Energy Physics - Experiment", "hep-ex", "Description coming soon"),
      ArxivCategoryData("High Energy Physics - Lattice", "hep-lat", "Lattice field theory. Phenomenology from lattice field theory. Algorithms for lattice field theory. Hardware for lattice field theory."),
      ArxivCategoryData("High Energy Physics - Phenomenology", "hep-ph", "Theoretical particle physics and its interrelation with experiment. Prediction of particle physics observables: models, effective field theories, calculation techniques. Particle physics: analysis of theory through experimental results."),
      ArxivCategoryData("High Energy Physics - Theory", "hep-th", "Formal aspects of quantum field theory. String theory, supersymmetry and supergravity."),
      ArxivCategoryData("Mathematical Physics", "math-ph", "Articles in this category focus on areas of research that illustrate the application of mathematics to problems in physics, develop mathematical methods for such applications, or provide mathematically rigorous formulations of existing physical theories. Submissions to math-ph should be of interest to both physically oriented mathematicians and mathematically oriented physicists; submissions which are primarily of interest to theoretical physicists or to mathematicians should probably be directed to the respective physics/math categories"),
      ArxivCategoryData("Nuclear Experiment", "nucl-ex", "Nuclear Experiment Results from experimental nuclear physics including the areas of fundamental interactions, measurements at low- and medium-energy, as well as relativistic heavy-ion collisions. Does not include: detectors and instrumentation nor analysis methods to conduct experiments; descriptions of experimental programs (present or future); comments on published results"),
      ArxivCategoryData("Nuclear Theory", "nucl-th", "Nuclear Theory Theory of nuclear structure covering wide area from models of hadron structure to neutron stars. Nuclear equation of states at different external conditions. Theory of nuclear reactions including heavy-ion reactions at low and high energies. It does not include problems of data analysis, physics of nuclear reactors, problems of safety, reactor construction"),
      ArxivCategoryData("Quantum Physics", "quant-ph", "Description coming soon"),
    )

    private val categoryNonlinearSciences = listOf(
      ArxivCategoryData("Adaptation and Self-Organizing Systems", "nlin.AO", "Adaptation, self-organizing systems, statistical physics, fluctuating systems, stochastic processes, interacting particle systems, machine learning"),
      ArxivCategoryData("Chaotic Dynamics", "nlin.CD", "Dynamical systems, chaos, quantum chaos, topological dynamics, cycle expansions, turbulence, propagation"),
      ArxivCategoryData("Cellular Automata and Lattice Gases", "nlin.CG", "Computational methods, time series analysis, signal processing, wavelets, lattice gases"),
      ArxivCategoryData("Pattern Formation and Solitons", "nlin.PS", "Pattern formation, coherent structures, solitons"),
      ArxivCategoryData("Exactly Solvable and Integrable Systems", "nlin.SI", "Exactly solvable systems, integrable PDEs, integrable ODEs, Painleve analysis, integrable discrete maps, solvable lattice models, integrable quantum systems"),
    )


  }

  enum class ArxivCategory(val category: String) {
    AstroPhysics("Astro-Physics"),
    ComputerScience("Computer Science"),
    CondensedMatter("Condensed Matter"),
    Economics("Economics"),
    ElectricalEngineeringAndSystemService("Electrical Engineering and Systems Science"),
    Mathematics("Mathematics"),
    Physics("Physics"),
    PhysicsOther("Physics Other"),
    QuantitativeBiology("Quantitative Biology"),
    QuantitativeFinance("Quantitative Finance"),
    Statistics("Statistics"),
    NonlinearSciences("Nonlinear Sciences"),
  }

  data class ArxivCategoryData(val category: String, val code: String, val description: String?)

}




