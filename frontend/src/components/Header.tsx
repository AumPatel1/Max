import MaxLogo from "./MaxLogo";
import SearchBar from "./SearchBar";
import UserAuth from "./UserAuth";

export default function Header() {
  return (
    <header className="bg-[#000000] grid grid-cols-3 items-center">
      <MaxLogo />

      <SearchBar />

      <UserAuth />
    </header>
  );
}
