import { useState, useEffect, useRef } from "react";
import MaxLogo from "../Header/MaxLogo";
export default function MobileSearch() {
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [shouldShake, setShouldShake] = useState(false);

  const handleFocus = () => {
    setShouldShake(true);
    setTimeout(() => setShouldShake(false), 500);
  };

  useEffect(() => {
    if (isSearchOpen) {
      setShouldShake(true);
      const timer = setTimeout(() => setShouldShake(false), 500);
      return () => clearTimeout(timer);
    }
  }, [isSearchOpen]);
  return (
    <>
      <button
        onClick={() => setIsSearchOpen(!isSearchOpen)}
        className="sm:flex md:hidden text-[#ffffff]/60 hover:text-[#48CAE4] hover:bg-[#ffffff]/10 transition-colors rounded-[50px] p-2 cursor-pointer "
      >
        <svg
          width="25"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M19.6 21L13.3 14.7C12.8 15.1 12.225 15.4167 11.575 15.65C10.925 15.8833 10.2333 16 9.5 16C7.68333 16 6.14583 15.3708 4.8875 14.1125C3.62917 12.8542 3 11.3167 3 9.5C3 7.68333 3.62917 6.14583 4.8875 4.8875C6.14583 3.62917 7.68333 3 9.5 3C11.3167 3 12.8542 3.62917 14.1125 4.8875C15.3708 6.14583 16 7.68333 16 9.5C16 10.2333 15.8833 10.925 15.65 11.575C15.4167 12.225 15.1 12.8 14.7 13.3L21 19.6L19.6 21ZM9.5 14C10.75 14 11.8125 13.5625 12.6875 12.6875C13.5625 11.8125 14 10.75 14 9.5C14 8.25 13.5625 7.1875 12.6875 6.3125C11.8125 5.4375 10.75 5 9.5 5C8.25 5 7.1875 5.4375 6.3125 6.3125C5.4375 7.1875 5 8.25 5 9.5C5 10.75 5.4375 11.8125 6.3125 12.6875C7.1875 13.5625 8.25 14 9.5 14Z"
            fill="currentColor"
          />
        </svg>
      </button>
      {isSearchOpen && (
        <div className="z-50 absolute top-0 left-0 w-full h-full bg-[#000000]  items-start pt-3 gap-5">
          <div className="flex justify-between items-center px-3">
            <MaxLogo />
            <button onClick={() => setIsSearchOpen(false)}>
              <svg
                className="h-10"
                aria-hidden="true"
                focusable="false"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                fill="none"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                <line x1="10" y1="10" x2="14" y2="14" />
                <line x1="14" y1="10" x2="10" y2="14" />
              </svg>
            </button>
          </div>
          <div className="w-full flex justify-center mt-5 px-4">
            <div className="w-full max-w-2xl flex items-center gap-3 px-4 py-3 rounded-[50px] bg-[#242423]/50 border border-[#ffffff]/10 hover:border-[#48CAE4]/50 focus-within:border-[#48CAE4] transition-all duration-200 group">
              <svg
                className={`w-5 h-5 text-gray-400 group-focus-within:text-[#48CAE4] ${shouldShake ? "animate-shake" : ""}`}
                viewBox="0 0 24 24"
                fill="none"
              >
                <path
                  d="M19.6 21L13.3 14.7C12.8 15.1 12.225 15.4167 11.575 15.65C10.925 15.8833 10.2333 16 9.5 16C7.68333 16 6.14583 15.3708 4.8875 14.1125C3.62917 12.8542 3 11.3167 3 9.5C3 7.68333 3.62917 6.14583 4.8875 4.8875C6.14583 3.62917 7.68333 3 9.5 3C11.3167 3 12.8542 3.62917 14.1125 4.8875C15.3708 6.14583 16 7.68333 16 9.5C16 10.2333 15.8833 10.925 15.65 11.575C15.4167 12.225 15.1 12.8 14.7 13.3L21 19.6L19.6 21ZM9.5 14C10.75 14 11.8125 13.5625 12.6875 12.6875C13.5625 11.8125 14 10.75 14 9.5C14 8.25 13.5625 7.1875 12.6875 6.3125C11.8125 5.4375 10.75 5 9.5 5C8.25 5 7.1875 5.4375 6.3125 6.3125C5.4375 7.1875 5 8.25 5 9.5C5 10.75 5.4375 11.8125 6.3125 12.6875C7.1875 13.5625 8.25 14 9.5 14Z"
                  fill="currentColor"
                />
              </svg>
              <input
                placeholder="Search..."
                autoFocus
                onFocus={handleFocus}
                className="flex-1 bg-transparent text-[#ffffff] border-none focus:outline-none placeholder:text-gray-400 focus:placeholder:text-gray-500 transition-all"
              />
            </div>
          </div>
        </div>
      )}
    </>
  );
}
