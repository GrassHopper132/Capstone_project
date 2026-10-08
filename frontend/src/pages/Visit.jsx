import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  listOpenExhibitions,
  getOpenExhibition,
  listArchive,
  getArchiveExhibition,
  longDate,
  directionsUrl,
  MUSEUM,
} from "../api/visit";
import { useAuth } from "../context/AuthContext";
import "../styles/visit.css";

/** ISO date strings compare correctly as plain strings, so no Date maths. */
function statusLine(show, today) {
  if (show.startDate > today) return `Opens ${longDate(show.startDate)}`;
  if (show.endDate < today) return `Closed ${longDate(show.endDate)}`;
  return `Through ${longDate(show.endDate)}`;
}

export default function Visit() {
  const { isAuthenticated, user, signOut } = useAuth();
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    async function load() {
      try {
        /*
         * A signed-in visitor gets the whole programme; an anonymous one gets
         * only what is hanging today. These are different endpoints, not the
         * same endpoint with a filter, so the server decides what is visible
         * and the page cannot widen it by asking differently.
         *
         * The archive is paged, so it arrives wrapped in `content`.
         */
        const summaries = isAuthenticated
            ? (await listArchive())?.content ?? []
            : (await listOpenExhibitions()) ?? [];


        const fetchOne = isAuthenticated ? getArchiveExhibition : getOpenExhibition;
        const full = await Promise.all(summaries.map((s) => fetchOne(s.id)));
        if (!cancelled) setShows(full);
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [isAuthenticated]);

  const today = new Date().toLocaleDateString("en-CA");
  const openNow = shows.filter((s) => s.startDate <= today && s.endDate >= today);
  const hero = openNow.flatMap((s) => s.artifacts ?? []).find((a) => a.imageUrl);

  return (
      <div className="visit">
        <header className="visit-bar">
          <span className="visit-wordmark">{MUSEUM.name}</span>
          <nav>
            {isAuthenticated ? (
                <>
                  <span className="visit-quiet">{user?.fullName ?? user?.email}</span>
                  {" "}
                  <button type="button" className="visit-linkish" onClick={signOut}>
                    Sign out
                  </button>
                </>
            ) : (
                <Link to="/login">Sign in</Link>
            )}
          </nav>
        </header>

        <main className="visit-main">
          {hero && (
              <figure className="visit-hero">
                <img src={hero.imageUrl} alt={hero.title} loading="eager" />
                <figcaption>{hero.title}, on view now</figcaption>
              </figure>
          )}

          <section className="visit-intro">
            <h1>{isAuthenticated ? "The full programme" : "What you can see today"}</h1>
            <p>
              {isAuthenticated
                  ? "Every show on record: closed, open and announced. Objects move to the floor when a show opens and return to storage when it closes."
                  : "Every object listed here is currently on the floor. Shows close on the dates given, and objects return to storage when they do."}
            </p>
          </section>

          {loading && <p className="visit-quiet">Loading today&rsquo;s galleries.</p>}

          {error && (
              <p className="visit-quiet">
                The gallery listing is unavailable right now. Try again shortly.
              </p>
          )}

          {!loading && !error && shows.length === 0 && (
              <p className="visit-quiet">
                Nothing is open to the public today. The next show is being installed.
              </p>
          )}

          {shows.map((show) => (
              <section className="visit-show" key={show.id}>
                <div className="visit-show-head">
                  <h2>{show.title}</h2>
                  <p className="visit-show-meta">
                    {show.gallery}
                    <br />
                    {statusLine(show, today)}
                  </p>
                </div>

                {(show.artifacts ?? []).length === 0 && (
                    <p className="visit-quiet">Objects for this show are still being installed.</p>
                )}

                {(show.artifacts ?? []).map((a) => (
                    <article className="visit-object" key={a.artifactId}>
                      <div className="visit-object-image">
                        {a.imageUrl ? (
                            <img src={a.imageUrl} alt={a.title} loading="lazy" />
                        ) : (
                            <div className="visit-noimage">No photograph yet</div>
                        )}
                      </div>
                      <div>
                        <h3>{a.title}</h3>
                        <dl>
                          <dt>Accession</dt>
                          <dd>{a.accessionNumber}</dd>
                        </dl>
                      </div>
                    </article>
                ))}
              </section>
          ))}

          <footer className="visit-foot">
            <div>
              <h2>Plan your visit</h2>
              <p>{MUSEUM.address}</p>
              <p>{MUSEUM.hours}</p>
              <a className="visit-directions" href={directionsUrl()} target="_blank" rel="noreferrer">
                Get directions
              </a>
            </div>
            <div>
              <h2>{isAuthenticated ? "You have the full programme" : "See the full programme"}</h2>
              <p>
                {isAuthenticated
                    ? "Shows that have closed and shows announced but not yet installed are listed above. Sign out to see the public page."
                    : "A free account opens the archive: shows that have closed, and shows announced but not yet installed."}
              </p>
              {!isAuthenticated && (
                  <p>
                    <Link to="/register">Create an account</Link> or <Link to="/login">sign in</Link>
                  </p>
              )}
            </div>
          </footer>
        </main>
      </div>
  );
}