interface Candidate {
  name: string;
  image: string;
  payout: string;
  odds: string;
  color: string;
}

interface MarketCardProps {
  title: string;
  candidates: Candidate[];
  volume: string;
  marketCount: string;
  newsText: string;
  currentIndex?: number;
  totalCards?: number;
}

export default function MarketCard({
  title,
  candidates,
  volume,
  marketCount,
  newsText,
  currentIndex = 1,
  totalCards = 7,
}: MarketCardProps) {
  return (
    <div className="bg-[#1a1d1f] rounded-2xl p-6 md:p-8 border border-[#2a2d2f] max-w-225 mx-auto">
      {/* Header with navigation */}
      <div className="flex justify-between items-start mb-6">
        <h1 className="text-white text-2xl md:text-3xl font-bold">{title}</h1>
        <div className="flex items-center gap-3">
          <button className="w-10 h-10 rounded-full bg-[#2a2d2f] flex items-center justify-center hover:bg-[#3a3d3f] transition-colors">
            <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <span className="text-gray-400 text-sm">
            {currentIndex} of {totalCards}
          </span>
          <button className="w-10 h-10 rounded-full bg-[#2a2d2f] flex items-center justify-center hover:bg-[#3a3d3f] transition-colors">
            <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>

      {/* Mobile/Tablet Layout (< 768px) */}
      <div className="md:hidden">
        <div className="flex justify-between text-sm text-gray-400 mb-4">
          <span>Market</span>
          <div className="flex gap-8">
            <span>Pays out</span>
            <span>Odds</span>
          </div>
        </div>

        {/* Candidates */}
        {candidates.map((candidate, index) => (
          <div key={index} className="flex items-center justify-between mb-4 pb-4 border-b border-[#2a2d2f] last:border-b-0">
            <div className="flex items-center gap-3">
              <img
                src={candidate.image}
                alt={candidate.name}
                className="w-10 h-10 rounded-full"
              />
              <div>
                <h3 className="text-white font-semibold">{candidate.name}</h3>
                <div className={`h-1 w-16 rounded-full mt-1`} style={{ backgroundColor: candidate.color }}></div>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-gray-400 text-sm">{candidate.payout}</span>
              <span className="text-white font-semibold bg-[#2a2d2f] px-4 py-2 rounded-full border border-[#48CAE4]">
                {candidate.odds}
              </span>
            </div>
          </div>
        ))}

        {/* Volume and markets */}
        <div className="flex justify-between mt-6 text-sm border-t border-[#2a2d2f] pt-4">
          <span className="text-white font-semibold">{volume} vol</span>
          <span className="text-gray-400">{marketCount} markets</span>
        </div>

        {/* News section */}
        <div className="mt-6 border-t border-[#2a2d2f] pt-4">
          <p className="text-gray-400 text-sm leading-relaxed">
            <span className="text-white font-semibold">News</span> {newsText}
          </p>
        </div>
      </div>

      {/* Desktop Layout (>= 768px) */}
      <div className="hidden md:grid md:grid-cols-2 gap-8">
        {/* Left side - Market info */}
        <div>
          <div className="flex gap-4 text-sm text-gray-400 mb-4">
            <span>Market</span>
            <span>Pays out</span>
            <span>Odds</span>
          </div>

          {/* Candidates */}
          {candidates.map((candidate, index) => (
            <div key={index} className="flex items-center justify-between mb-4 pb-4 border-b border-[#2a2d2f] last:border-b-0">
              <div className="flex items-center gap-3 flex-1">
                <img
                  src={candidate.image}
                  alt={candidate.name}
                  className="w-10 h-10 rounded-full"
                />
                <div>
                  <h3 className="text-white font-semibold">{candidate.name}</h3>
                  <div className={`h-1 w-16 rounded-full mt-1`} style={{ backgroundColor: candidate.color }}></div>
                </div>
              </div>
              <span className="text-gray-400 mx-8">{candidate.payout}</span>
              <span className="text-white font-semibold bg-[#2a2d2f] px-4 py-2 rounded-full border border-[#48CAE4]">
                {candidate.odds}
              </span>
            </div>
          ))}

          {/* Volume and markets */}
          <div className="flex justify-between mt-6 text-sm">
            <span className="text-white font-semibold">{volume} vol</span>
            <span className="text-gray-400">{marketCount} markets</span>
          </div>

          {/* News section */}
          <div className="mt-6">
            <p className="text-gray-400 text-sm leading-relaxed">
              <span className="text-white font-semibold">News</span> {newsText}
            </p>
          </div>
        </div>

        {/* Right side - Chart and legend */}
        <div>
          {/* Legend */}
          <div className="flex justify-between items-start mb-4">
            <div className="flex flex-wrap gap-4">
              {candidates.map((candidate, index) => (
                <div key={index} className="flex items-center gap-2">
                  <div className={`w-2 h-2 rounded-full`} style={{ backgroundColor: candidate.color }}></div>
                  <span className="text-gray-400 text-sm">{candidate.name}</span>
                  <span className="text-white text-sm font-semibold">{candidate.odds}</span>
                </div>
              ))}
            </div>
            <span className="text-[#48CAE4] font-bold text-xl">Max</span>
          </div>

          {/* Chart placeholder */}
          <div className="bg-[#0f1113] rounded-lg p-4 h-64 relative">
            <svg className="w-full h-full" viewBox="0 0 400 200" preserveAspectRatio="none">
              {/* Grid lines */}
              {[0, 20, 40, 60, 80, 100].map((y) => (
                <line
                  key={y}
                  x1="0"
                  y1={200 - (y * 2)}
                  x2="400"
                  y2={200 - (y * 2)}
                  stroke="#2a2d2f"
                  strokeWidth="1"
                />
              ))}
              
              {/* Sample chart lines */}
              <polyline
                points="0,120 50,115 100,110 150,80 200,75 250,70 300,85 350,90 400,95"
                fill="none"
                stroke={candidates[0]?.color || "#4a9eff"}
                strokeWidth="2"
              />
              <polyline
                points="0,140 50,135 100,130 150,100 200,95 250,90 300,100 350,105 400,110"
                fill="none"
                stroke={candidates[1]?.color || "#00d4aa"}
                strokeWidth="2"
              />
              {candidates[2] && (
                <polyline
                  points="0,160 50,158 100,155 150,145 200,140 250,135 300,150 350,165 400,170"
                  fill="none"
                  stroke={candidates[2].color}
                  strokeWidth="2"
                />
              )}
            </svg>
            
            {/* Y-axis labels */}
            <div className="absolute left-0 top-0 h-full flex flex-col justify-between text-xs text-gray-500 -ml-8">
              <span>40%</span>
              <span>30%</span>
              <span>20%</span>
              <span>10%</span>
              <span>0%</span>
            </div>
            
            {/* X-axis labels */}
            <div className="absolute bottom-0 left-0 w-full flex justify-between text-xs text-gray-500 -mb-6 px-4">
              <span>May</span>
              <span>Jul</span>
              <span>Oct</span>
              <span>Dec</span>
              <span>Mar</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
