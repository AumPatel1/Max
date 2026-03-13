import { useRef } from "react";

const categories = [
  "Trending",
  "Politics",
  "Sports",
  "Culture",
  "Crypto",
  "Climate",
  "Economics",
  "Mentions",
  "Companies",
  "Financials",
  "Tech & Science",
];

export default function CategoryBar() {
  const scrollRef = useRef<HTMLDivElement>(null);

  const handleWheel = (e: React.WheelEvent) => {
    if (scrollRef.current) {
      e.preventDefault();
      scrollRef.current.scrollLeft += e.deltaY;
    }
  };

  return (
    <div 
      ref={scrollRef}
      onWheel={handleWheel}
      className="w-full flex gap-7 pt-0.5 pb-3 bg-[#000000]/50 overflow-x-auto max-w-330 mx-auto scrollbar-hide scroll-smooth md:px-6"
    >
      {categories.map((category) => (
        <a
          key={category}
          href={`/category/${category.toLowerCase()}`}
          className="text-[#ffffff]/60 font-semibold rounded-full hover:text-[#ffffff] transition-all duration-200 whitespace-nowrap active:scale-95"
        >
          {category}
        </a>
      ))}
    </div>
  );
}