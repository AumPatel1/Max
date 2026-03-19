import { createSlice } from "@reduxjs/toolkit";

interface FooterLink {
  label: string;
  href: string;
}

interface FooterSection {
  title: string;
  links: FooterLink[];
}

interface FooterState {
  sections: FooterSection[];
  disclaimer: string;
  copyright: string;
}

const initialState: FooterState = {
  sections: [
    {
      title: "Company",
      links: [
        { label: "Blog", href: "#" },
        { label: "Careers", href: "#" },
        { label: "Privacy Policy", href: "#" },
        { label: "Contest Rules", href: "#" },
        { label: "Data Terms of Service", href: "#" },
        { label: "Company", href: "#" },
        { label: "Brand Kit", href: "#" },
      ],
    },
    {
      title: "Social",
      links: [
        { label: "X (Twitter)", href: "#" },
        { label: "LinkedIn", href: "#" },
        { label: "Discord", href: "#" },
        { label: "Instagram", href: "#" },
        { label: "Reddit", href: "#" },
        { label: "TikTok", href: "#" },
      ],
    },
    {
      title: "Product",
      links: [
        { label: "Help Center", href: "#" },
        { label: "API", href: "#" },
        { label: "FAQ", href: "#" },
        { label: "FAQ for Finance Professionals", href: "#" },
        { label: "Regulatory", href: "#" },
        { label: "Trading Hours", href: "#" },
        { label: "Fee Schedule", href: "#" },
        { label: "Trading Prohibitions", href: "#" },
        { label: "Incentive Program", href: "#" },
        { label: "Research", href: "#" },
        { label: "Institutional Trading", href: "#" },
        { label: "Responsible Trading", href: "#" },
        { label: "Market Integrity", href: "#" },
      ],
    },
  ],
  copyright: "© 2026 Max Inc.",
  disclaimer:
    "Trading on Max involves risk and may not be appropriate for all. Members risk losing their cost to enter any transaction, including fees. You should carefully consider whether trading on Max is appropriate for you in light of your investment experience and financial resources. Any trading decisions you make are solely your responsibility and at your own risk. Information is provided for convenience only on an \"AS IS\" basis. Past performance is not necessarily indicative of future results. Max is subject to Canadian regulatory oversight by the CSA.",
};

const footerSlice = createSlice({
  name: "footer",
  initialState,
  reducers: {},
});

export default footerSlice.reducer;
