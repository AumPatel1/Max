const BASE_URL = "http://localhost:8080/api";

export async function fetchHomeData() {
  const res = await fetch(`${BASE_URL}/home`);
  if (!res.ok) throw new Error("Failed to fetch home data");
  return res.json();
}
