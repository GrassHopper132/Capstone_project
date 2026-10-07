import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  listOpenExhibitions,
  getOpenExhibition,
  longDate,
  directionsUrl,
  MUSEUM,
} from "../api/visit";
import "../styles/visit.css";

export default function Visit() {
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const open = await listOpenExhibitions();
        const full = await Promise.all((open ?? []).map((s) => getOpenExhibition(s.id)));
        if (!cancelled) setShows(full);
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, []);

  const hero = shows.flatMap((s) => s.artifacts ?? []).find((a) => a.imageUrl);

  return (
    <div className="visit">
      <header className="visit-bar">
        <span className="visit-wordmark">{MUSEUM.name}</span>
        <nav>
          <Link to="/login">Sign in</Link>
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
          <h1>What you can see today</h1>
          <p>
            Every object listed here is currently on the floor. Shows close on the
            dates given, and objects return to storage when they do.
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
                Through {longDate(show.endDate)}
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
            <h2>See the full programme</h2>
            <p>
              A free account opens the archive: shows that have closed, and shows
              announced but not yet installed.
            </p>
            <p>
              <Link to="/register">Create an account</Link> or <Link to="/login">sign in</Link>
            </p>
          </div>
        </footer>
      </main>
    </div>
  );
}