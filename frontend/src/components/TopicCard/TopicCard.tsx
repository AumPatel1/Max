interface Option {
  label: string;
  payout: string;
  odds: string;
  color?: string;
}

interface TopicCardProps {
  category: string;
  categoryIcon: React.ReactNode;
  title: string;
  date: string;
  options: Option[];
  volume: string;
  marketCount: string;
}

export default function TopicCard({
  category,
  categoryIcon,
  title,
  date,
  options,
  volume,
  marketCount,
}: TopicCardProps) {
  return (
    <div className="bg-[#1a1d1f] rounded-xl p-6 border border-[#2a2d2f] hover:border-[#3a3d3f] transition-all cursor-pointer">
      {/* Category Header */}
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 bg-[#2a2d2f] rounded-lg flex items-center justify-center">
          {categoryIcon}
        </div>
        <span className="text-gray-400 text-xs uppercase tracking-wider font-semibold">
          {category}
        </span>
      </div>

      {/* Title */}
      <h3 className="text-white font-semibold text-lg mb-2 leading-tight">
        {title}
      </h3>

      {/* Date */}
      <p className="text-gray-500 text-sm mb-4">{date}</p>

      {/* Options */}
      <div className="space-y-3 mb-4">
        {options.map((option, index) => (
          <div key={index} className="flex items-center justify-between">
            <div className="flex-1">
              <div className="text-white text-sm font-medium mb-1">
                {option.label}
              </div>
              <div
                className="h-0.5 rounded-full"
                style={{
                  width: option.odds,
                  backgroundColor: option.color || "#48CAE4",
                }}
              ></div>
            </div>
            <div className="flex items-center gap-3 ml-4">
              <span className="text-gray-400 text-sm">{option.payout}</span>
              <span className="text-white text-sm font-semibold bg-[#0f1113] px-3 py-1.5 rounded-full border border-[#2a2d2f]">
                {option.odds}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div className="flex justify-between items-center text-sm pt-4 border-t border-[#2a2d2f]">
        <span className="text-white font-semibold">{volume} vol</span>
        <span className="text-gray-400">{marketCount} markets</span>
      </div>
    </div>
  );
}
