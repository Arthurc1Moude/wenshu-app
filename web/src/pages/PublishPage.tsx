import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { Image, X, Hash, Smile, MapPin, Loader2 } from 'lucide-react';
import { useStore } from '@/store';

export default function PublishPage() {
  const [content, setContent] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [pendingFiles, setPendingFiles] = useState<{ file: File; preview: string; uploading: boolean }[]>([]);
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState('');
  const [showTagInput, setShowTagInput] = useState(false);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { createPost, isLoggedIn, uploadImage, showToast } = useStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) navigate('/login');
  }, [isLoggedIn, navigate]);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    const filesToUpload = Array.from(files).slice(0, 9 - images.length - pendingFiles.length);
    if (filesToUpload.length === 0) return;

    const newPending = filesToUpload.map(file => ({
      file,
      preview: URL.createObjectURL(file),
      uploading: true
    }));
    setPendingFiles(prev => [...prev, ...newPending]);
    setUploading(true);

    for (const item of newPending) {
      const url = await uploadImage(item.file);
      if (url) {
        setImages(prev => [...prev, url]);
      } else {
        showToast('图片上传失败', 'error');
      }
      setPendingFiles(prev => prev.filter(p => p.preview !== item.preview));
      URL.revokeObjectURL(item.preview);
    }
    setUploading(false);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const removeImage = (i: number) => {
    setImages(images.filter((_, idx) => idx !== i));
  };

  const addTag = () => {
    const t = tagInput.trim();
    if (t && !tags.includes(t) && tags.length < 5) {
      setTags([...tags, t]);
      setTagInput('');
      setShowTagInput(false);
    }
  };

  const removeTag = (t: string) => setTags(tags.filter(x => x !== t));

  const handlePublish = async () => {
    if (!content.trim() && images.length === 0) return;
    if (uploading || pendingFiles.length > 0) {
      showToast('请等待图片上传完成', 'info');
      return;
    }
    setLoading(true);
    const res = await createPost(content.trim(), images, tags);
    setLoading(false);
    if (res) {
      navigate(-1);
    }
  };

  const canPublish = (content.trim().length > 0 || images.length > 0) && !uploading && pendingFiles.length === 0;

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="发布帖子" showBack rightElement={
        <button
          onClick={handlePublish}
          disabled={!canPublish || loading}
          className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
            canPublish && !loading ? 'bg-black text-white active:scale-95' : 'bg-gray-200 text-text-tertiary'
          }`}
        >
          {loading ? '发布中...' : '发布'}
        </button>
      } />

      <div className="p-4">
        <textarea
          ref={textareaRef}
          value={content}
          onChange={e => setContent(e.target.value)}
          placeholder="分享你的生活..."
          className="w-full min-h-[180px] text-base outline-none resize-none placeholder:text-text-hint"
          autoFocus
        />

        {images.length > 0 && (
          <div className="grid grid-cols-3 gap-2 mt-4">
            {images.map((img, i) => (
              <div key={i} className="relative aspect-square">
                <img src={img} alt="" className="w-full h-full object-cover rounded-lg" />
                <button
                  onClick={() => removeImage(i)}
                  className="absolute top-1 right-1 w-5 h-5 bg-black/60 rounded-full flex items-center justify-center"
                >
                  <X className="w-3 h-3 text-white" />
                </button>
              </div>
            ))}
            {pendingFiles.map((p, i) => (
              <div key={'pending-'+i} className="relative aspect-square">
                <img src={p.preview} alt="" className="w-full h-full object-cover rounded-lg opacity-60" />
                <div className="absolute inset-0 flex items-center justify-center bg-black/20 rounded-lg">
                  <Loader2 className="w-6 h-6 text-white animate-spin" />
                </div>
              </div>
            ))}
          </div>
        )}

        {images.length === 0 && pendingFiles.length > 0 && (
          <div className="grid grid-cols-3 gap-2 mt-4">
            {pendingFiles.map((p, i) => (
              <div key={'pending-'+i} className="relative aspect-square">
                <img src={p.preview} alt="" className="w-full h-full object-cover rounded-lg opacity-60" />
                <div className="absolute inset-0 flex items-center justify-center bg-black/20 rounded-lg">
                  <Loader2 className="w-6 h-6 text-white animate-spin" />
                </div>
              </div>
            ))}
          </div>
        )}

        {images.length + pendingFiles.length < 9 && (
          <button
            onClick={() => fileInputRef.current?.click()}
            className="mt-4 w-20 h-20 border-2 border-dashed border-divider rounded-lg flex flex-col items-center justify-center text-text-tertiary active:bg-gray-50 transition-colors"
          >
            <Image className="w-6 h-6" />
            <span className="text-xs mt-1">{images.length + pendingFiles.length}/9</span>
          </button>
        )}
        <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleImageUpload} />

        {tags.length > 0 && (
          <div className="flex gap-2 flex-wrap mt-4">
            {tags.map(t => (
              <span key={t} className="inline-flex items-center gap-1 bg-gray-100 text-text-primary text-sm px-3 py-1 rounded-full">
                #{t}
                <button onClick={() => removeTag(t)}><X className="w-3 h-3" /></button>
              </span>
            ))}
          </div>
        )}

        {showTagInput && (
          <div className="mt-3 flex gap-2">
            <div className="flex-1 flex items-center bg-gray-100 rounded-lg px-3">
              <Hash className="w-4 h-4 text-text-tertiary" />
              <input
                type="text"
                value={tagInput}
                onChange={e => setTagInput(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && addTag()}
                placeholder="添加话题标签"
                className="flex-1 py-2 px-2 bg-transparent outline-none text-sm"
                autoFocus
              />
            </div>
            <button onClick={addTag} className="px-4 py-2 bg-black text-white text-sm rounded-lg">添加</button>
          </div>
        )}

        <div className="mt-6 border-t border-divider pt-3">
          <div className="flex gap-4">
            <button onClick={() => { setShowTagInput(!showTagInput); if (!showTagInput) setTagInput(''); }} className="flex items-center gap-2 text-sm text-text-secondary active:text-black">
              <Hash className="w-5 h-5" />话题
            </button>
            <button className="flex items-center gap-2 text-sm text-text-secondary active:text-black">
              <Smile className="w-5 h-5" />表情
            </button>
            <button className="flex items-center gap-2 text-sm text-text-secondary active:text-black">
              <MapPin className="w-5 h-5" />位置
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
