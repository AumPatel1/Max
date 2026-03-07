export default function MaxLogo() {
  return (
    <div className="flex justify-end">
      <button className="bg-[#000000] text-[#48CAE4] font-bold py-2 px-5 mx-2 rounded-[50px] border border-[#ffffff]/50 hover:bg-[#ffffff]/10 hover:cursor-pointer active:scale-95 transition-all duration-200 ">
        Log in
      </button>
      <button className="bg-[#48CAE4] text-[#000000] font-bold py-2 px-5 rounded-[50px] hover:bg-[#48CAE4]/80 hover:cursor-pointer active:scale-95 transition-all duration-200">
        Sign up
      </button>
    </div>
  );
}
