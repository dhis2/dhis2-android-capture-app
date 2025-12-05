import { useState, useEffect } from 'react';

// Drupal JSON:API の型定義
interface DrupalFile {
  id: string;
  type: string;
  attributes: {
    uri: {
      url: string;
    };
  };
}

interface DrupalMedia {
  id: string;
  type: string;
  attributes: {
    name: string;
  };
  relationships: {
    field_media_video_file: {
      data: {
        id: string;
        type: string;
      };
    };
  };
}

interface DrupalApiResponse {
  data: DrupalMedia[];
  included: DrupalFile[];
}

interface Video {
  id: string;
  name: string;
  url: string;
}

export default function Videos() {
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [rawData, setRawData] = useState<DrupalApiResponse | null>(null);
  const [showJson, setShowJson] = useState<boolean>(false);

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const res = await fetch(
          "https://drupal.ddev.site/jsonapi/media/video?include=field_media_video_file"
        );

        if (!res.ok) {
          throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data: DrupalApiResponse = await res.json();
        
        // 取得したJSONデータを保存
        setRawData(data);

        // included 配列からファイルID → URLのマップを作る
        const filesMap: Record<string, string> = {};
        data.included.forEach(item => {
          if (item.type === "file--file") {
            filesMap[item.id] = item.attributes.uri.url;
          }
        });

        // media data から動画情報を作成
        const videosData: Video[] = data.data.map(media => {
          const fileId = media.relationships.field_media_video_file.data.id;
          return {
            id: media.id,
            name: media.attributes.name,
            url: `https://drupal.ddev.site${filesMap[fileId]}`
          };
        });

        setVideos(videosData);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
        setError(errorMessage);
      } finally {
        setLoading(false);
      }
    };

    fetchVideos();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>Videos</h1>
      
      {rawData && (
        <div style={{ marginBottom: '20px' }}>
          <button
            onClick={() => setShowJson(!showJson)}
            style={{
              padding: '10px 20px',
              backgroundColor: '#61dafb',
              color: '#282c34',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            {showJson ? 'JSONを非表示' : 'JSONレスポンスを表示'}
          </button>
          
          {showJson && (
            <div style={{
              marginTop: '15px',
              padding: '15px',
              backgroundColor: '#f5f5f5',
              borderRadius: '4px',
              border: '1px solid #ddd',
              overflow: 'auto',
              maxHeight: '600px'
            }}>
              <h3 style={{ marginTop: 0 }}>APIレスポンス (JSON)</h3>
              <pre style={{
                margin: 0,
                fontSize: '12px',
                lineHeight: '1.5',
                whiteSpace: 'pre-wrap',
                wordWrap: 'break-word'
              }}>
                {JSON.stringify(rawData, null, 2)}
              </pre>
            </div>
          )}
        </div>
      )}
      
      {videos.length === 0 ? (
        <p>No videos found.</p>
      ) : (
        videos.map(video => (
          <div key={video.id} style={{ marginBottom: '30px' }}>
            <h3>{video.name}</h3>
            <video controls width="480">
              <source src={video.url} type="video/mp4" />
            </video>
          </div>
        ))
      )}
    </div>
  );
}

