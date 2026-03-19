import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";

export default function Footer() {
  const { sections, copyright, disclaimer } = useSelector(
    (state: RootState) => state.footer
  );

  return (
    <footer className="bg-[#111111] border-t border-[#ffffff]/10 mt-auto">
      <div className="max-w-330 mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
          {sections.map((section) => (
            <div key={section.title}>
              <h3 className="text-white font-semibold text-sm mb-4">
                {section.title}
              </h3>
              <ul className="space-y-3">
                {section.links.map((link) => (
                  <li key={link.label}>
                    <a
                      href={link.href}
                      className="text-[#aaaaaa] text-sm hover:text-white transition-colors duration-150"
                    >
                      {link.label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-[#ffffff]/10 mt-10 pt-6 space-y-3">
          <p className="text-[#aaaaaa] text-sm">{copyright}</p>
          <p className="text-[#666666] text-xs leading-relaxed">{disclaimer}</p>
        </div>
      </div>
    </footer>
  );
}
