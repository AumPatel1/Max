import MobileSearch from "../Search/MobileSearch";
export default function MaxLogo() {
  return (
    <div className="flex justify-end items-center gap-2 h-10">
      <MobileSearch />
      <button className="bg-[#000000] text-[#48CAE4] font-bold h-10 px-3 sm:px-5 mr-1 sm:mx-2 text-sm sm:text-base rounded-[50px] border border-[#ffffff]/50 hover:bg-[#ffffff]/10 hover:cursor-pointer active:scale-95 transition-all duration-200 ">
        Log in
      </button>
      <button className="bg-[#48CAE4] text-[#000000] font-bold h-10 px-3 sm:px-5 text-sm sm:text-base rounded-[50px] hover:bg-[#48CAE4]/80 hover:cursor-pointer active:scale-95 transition-all duration-200">
        Sign up
      </button>
    </div>
  );
}
