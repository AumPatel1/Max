import CategoryBar from "../Category/CategoryBar";
import MaxLogo from "./MaxLogo";
import SearchBar from "../Search/SearchBar";
import UserAuth from "./UserAuth";

export default function Header() {
  return (
    <header className="bg-[#000000] items-center border-b border-[#ffffff]/20 px-3">
      <div className="flex md:grid md:grid-cols-3 justify-between items-center max-w-330 mx-auto py-3 md:px-6 ">
        <MaxLogo />

        
        <SearchBar />
        

        <UserAuth />
      </div>
      <CategoryBar />
    </header>
  );
}
