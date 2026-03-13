interface InfoCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
}

export default function InfoCard({ icon, title, description }: InfoCardProps) {
  return (
    <div className="bg-[#0d2d2a] rounded-xl p-6 border border-[#1a4a44] hover:border-[#2a5a54] transition-all cursor-pointer group">
      <div className="flex items-start justify-between">
        <div className="flex gap-4">
          <div className="text-[#00d4aa] mt-1">{icon}</div>
          <div>
            <h3 className="text-white font-semibold mb-1">{title}</h3>
            <p className="text-gray-400 text-sm">{description}</p>
          </div>
        </div>
        <svg
          className="w-5 h-5 text-gray-400 group-hover:text-white transition-colors"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
        </svg>
      </div>
    </div>
  );
}
