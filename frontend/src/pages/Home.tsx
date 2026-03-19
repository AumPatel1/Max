import Header from "../components/Header/Header";
import Footer from "../components/Footer/Footer";
import MarketCard from "../components/MarketCard/MarketCard";
import InfoCard from "../components/InfoCard/InfoCard";
import TopicCard from "../components/TopicCard/TopicCard";

export default function Home() {
  const candidates = [
    {
      name: "Gavin Newsom",
      image: "https://via.placeholder.com/40",
      payout: "5.25x",
      odds: "18%",
      color: "#4a9eff",
    },
    {
      name: "J.D. Vance",
      image: "https://via.placeholder.com/40",
      payout: "5.25x",
      odds: "17%",
      color: "#00d4aa",
    },
    {
      name: "Marco Rubio",
      image: "https://via.placeholder.com/40",
      payout: "5.25x",
      odds: "18%",
      color: "#ffa500",
    },
  ];

  return (
    <div className="min-h-screen bg-[#000000]">
      <Header />
      
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Market Card */}
        <MarketCard
          title="2028 U.S. Presidential Election winner?"
          candidates={candidates}
          volume="$17,834,984"
          marketCount="22"
          newsText="Secretary of State Marco Rubio's chances in the race for the 2028 Republican presidential nomination have risen amid the U.S. conflict with Iran, Newsweek reports. Vice President JD Vance..."
          currentIndex={1}
          totalCards={7}
        />

        {/* Info Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-8">
          <InfoCard
            icon={
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z" />
              </svg>
            }
            title="Markets over Monopolies"
            description="How fair markets protect consumers"
          />
          <InfoCard
            icon={
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
              </svg>
            }
            title="Responsible Trading"
            description="Tools and tips for trading smart"
          />
          <InfoCard
            icon={
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
              </svg>
            }
            title="Market Integrity"
            description="Learn how Kalshi prevents insider trading"
          />
        </div>

        {/* Politics Section */}
        <div className="mt-12">
          <div className="flex items-center gap-2 mb-6">
            <h2 className="text-white text-2xl font-bold">Politics</h2>
            <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <TopicCard
              category="CONGRESS"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z" />
                </svg>
              }
              title="How long will the government shutdown last?"
              date="Feb 14 @ 10:00AM"
              options={[
                { label: "At least 60 days", payout: "2.05x", odds: "46%", color: "#48CAE4" },
                { label: "At least 55 days", payout: "1.65x", odds: "59%", color: "#48CAE4" },
              ]}
              volume="$4,199,600"
              marketCount="21"
            />

            <TopicCard
              category="CONGRESS"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z" />
                </svg>
              }
              title="Will members of Congress be banned from trading stocks?"
              date="Before Jan 21, 2029"
              options={[
                { label: "Before Jan 21, 2029", payout: "1.59x", odds: "56%", color: "#00d4aa" },
                { label: "Before 2027", payout: "7.14x", odds: "12%", color: "#00d4aa" },
              ]}
              volume="$222,525"
              marketCount="3"
            />

            <TopicCard
              category="TRUMP"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" />
                </svg>
              }
              title="Will Americans receive tariff stimulus checks?"
              date="Before 2027"
              options={[
                { label: "Before 2027", payout: "5.25x", odds: "18%", color: "#48CAE4" },
                { label: "Before August", payout: "10.4x", odds: "9%", color: "#4a9eff" },
              ]}
              volume="$1,334,816"
              marketCount="9"
            />

            <TopicCard
              category="INTERNATIONAL"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
                </svg>
              }
              title="What will the US tariff rate on China be on July 1?"
              date="Between 10% and 19.99%"
              options={[
                { label: "Between 10% and 19.99%", payout: "1.47x", odds: "69%", color: "#00d4aa" },
                { label: "Between 20% and 29.99%", payout: "4.35x", odds: "27%", color: "#00d4aa" },
              ]}
              volume="$36,656"
              marketCount="7"
            />
          </div>
        </div>

        {/* Elections Section */}
        <div className="mt-12">
          <div className="flex items-center gap-2 mb-6">
            <h2 className="text-white text-2xl font-bold">Elections</h2>
            <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <TopicCard
              category="PRESIDENTIAL"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z" />
                </svg>
              }
              title="Who will win the 2028 Presidential Election?"
              date="Nov 5, 2028"
              options={[
                { label: "Republican", payout: "1.85x", odds: "52%", color: "#ff4444" },
                { label: "Democrat", payout: "2.15x", odds: "45%", color: "#4444ff" },
              ]}
              volume="$8,500,000"
              marketCount="15"
            />

            <TopicCard
              category="SENATE"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z" />
                </svg>
              }
              title="Will Democrats control the Senate in 2026?"
              date="Jan 3, 2027"
              options={[
                { label: "Yes", payout: "2.85x", odds: "35%", color: "#4444ff" },
                { label: "No", payout: "1.55x", odds: "65%", color: "#ff4444" },
              ]}
              volume="$2,100,000"
              marketCount="8"
            />
          </div>
        </div>

        {/* Companies Section */}
        <div className="mt-12">
          <div className="flex items-center gap-2 mb-6">
            <h2 className="text-white text-2xl font-bold">Companies</h2>
            <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <TopicCard
              category="TECH"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20 18c1.1 0 1.99-.9 1.99-2L22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z" />
                </svg>
              }
              title="Will Apple stock hit $250 by end of 2025?"
              date="Dec 31, 2025"
              options={[
                { label: "Yes", payout: "2.25x", odds: "44%", color: "#00d4aa" },
                { label: "No", payout: "1.75x", odds: "56%", color: "#ff6b6b" },
              ]}
              volume="$3,450,000"
              marketCount="12"
            />

            <TopicCard
              category="AUTOMOTIVE"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z" />
                </svg>
              }
              title="Will Tesla deliver 2M vehicles in 2025?"
              date="Dec 31, 2025"
              options={[
                { label: "Yes", payout: "3.15x", odds: "32%", color: "#00d4aa" },
                { label: "No", payout: "1.45x", odds: "68%", color: "#ff6b6b" },
              ]}
              volume="$1,890,000"
              marketCount="6"
            />
          </div>
        </div>

        {/* Economics Section */}
        <div className="mt-12">
          <div className="flex items-center gap-2 mb-6">
            <h2 className="text-white text-2xl font-bold">Economics</h2>
            <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <TopicCard
              category="INFLATION"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z" />
                </svg>
              }
              title="Will inflation rate be below 2% by end of 2025?"
              date="Dec 31, 2025"
              options={[
                { label: "Yes", payout: "1.95x", odds: "51%", color: "#00d4aa" },
                { label: "No", payout: "2.05x", odds: "49%", color: "#ff6b6b" },
              ]}
              volume="$5,200,000"
              marketCount="18"
            />

            <TopicCard
              category="FED"
              categoryIcon={
                <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.91s4.18 1.39 4.18 3.91c-.01 1.83-1.38 2.83-3.12 3.16z" />
                </svg>
              }
              title="Will the Fed cut rates in Q2 2025?"
              date="Jun 30, 2025"
              options={[
                { label: "Yes", payout: "2.65x", odds: "38%", color: "#00d4aa" },
                { label: "No", payout: "1.62x", odds: "62%", color: "#ff6b6b" },
              ]}
              volume="$6,750,000"
              marketCount="22"
            />
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}